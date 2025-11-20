package com.wink.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wink.backend.dto.*;
import com.wink.backend.entity.ChatMessage;
import com.wink.backend.entity.ChatSession;
import com.wink.backend.repository.ChatMessageRepository;
import com.wink.backend.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final GeminiService geminiService;
    private final ChatMessageRepository messageRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public ChatService(ChatSessionRepository sessionRepo,
                       GeminiService geminiService,
                       ChatMessageRepository messageRepo) {
        this.sessionRepo = sessionRepo;
        this.geminiService = geminiService;
        this.messageRepo = messageRepo;
        this.restTemplate = new RestTemplate();
    }

    // =====================================================
    // 🚀 공통: 이전 세션 종료 (같은 타입만)
    // =====================================================
    private void endPreviousSessions(String type) {
        List<ChatSession> sessions = sessionRepo.findByTypeOrderByStartTimeDesc(type);
        for (ChatSession s : sessions) {
            if (!Boolean.TRUE.equals(s.getIsEnded())) {
                s.setIsEnded(true);
                s.setEndTime(LocalDateTime.now());
                sessionRepo.save(s);
            }
        }
    }

    // =====================================================
    // ① 나의 순간 시작 (→ 사용자 메시지 저장 + 바로 AI 응답 생성)
    // =====================================================
    // 반환 타입: AiResponseResponse
    public AiResponseResponse startMy(ChatStartMyRequest req) {

        endPreviousSessions("MY"); // 같은 타입 모두 종료

        ChatSession session = new ChatSession();
        session.setType("MY");
        session.setStartTime(LocalDateTime.now());
        session.setIsEnded(false);

        // 🔥 제미나이 기반 주제 생성
        String topic = geminiService.extractTopic(req.getInputText());
        session.setTopic(topic);

        sessionRepo.save(session); // 세션 저장

        // **변경: 1. 첫 사용자 메시지 저장**
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender("user");
        userMsg.setText(req.getInputText());
        messageRepo.save(userMsg); // 채팅 내용 저장 완료

        // **변경: 2. AI 응답 생성 요청**
        AiResponseRequest aiReq = new AiResponseRequest();
        aiReq.setSessionId(session.getId());
        aiReq.setInputText(req.getInputText());
        aiReq.setImageBase64(null);

        // **변경: 3. AI 응답을 받아 바로 반환**
        return generateAiResponse(aiReq);
    }

    // =====================================================
    // ② 공간의 순간 시작 (→ 사용자 메시지 저장 + 바로 AI 응답 생성)
    // =====================================================
    // 반환 타입: AiResponseResponse
    public AiResponseResponse startSpace(ChatStartSpaceRequest req) {

        endPreviousSessions("SPACE"); // 같은 타입 모두 종료

        ChatSession session = new ChatSession();
        session.setType("SPACE");
        session.setStartTime(LocalDateTime.now());
        session.setIsEnded(false);

        // [수정 완료] 주변 음악 요약 변수를 메서드 시작 부분에서 초기화
        String nearbySummary = ""; 
        if (req.getNearbyMusic() != null && !req.getNearbyMusic().isEmpty()) {
            nearbySummary = req.getNearbyMusic().stream()
                    .map(m -> m.getTitle() + " - " + m.getArtist())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        // 🔥 네가 만든 장소 기반 프롬프트 그대로 적용 (이제 nearbySummary 접근 가능)
        String prompt = String.format(
                "📍장소명: %s (%s)\n🎧 주변 음악: %s\n이 장소의 분위기와 어울리는 음악적 주제를 한 문장으로 요약해줘.",
                req.getLocation().getPlaceName(),
                req.getLocation().getAddress(),
                nearbySummary.isBlank() ? "정보 없음" : nearbySummary
        );

        String topic = geminiService.extractTopic(prompt);
        session.setTopic(topic);

        sessionRepo.save(session); // 세션 저장

        String initialText = String.format("%s에 왔습니다.", req.getLocation().getPlaceName());

        // **변경: 1. 첫 사용자 메시지 저장 (장소명)**
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender("user");
        userMsg.setText(initialText);
        messageRepo.save(userMsg); // 채팅 내용 저장 완료

        // **변경: 2. AI 응답 생성 요청**
        AiResponseRequest aiReq = new AiResponseRequest();
        aiReq.setSessionId(session.getId());
        aiReq.setInputText(initialText);
        aiReq.setImageBase64(null);

        // **변경: 3. AI 응답을 받아 바로 반환**
        return generateAiResponse(aiReq);
    }

    // =====================================================
    // ③ AI 응답 생성 (AI 메시지 저장)
    // =====================================================
    public AiResponseResponse generateAiResponse(AiResponseRequest req) {
        try {
            Long sessionId = req.getSessionId();
            ChatSession session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

            String topic = session.getTopic();

            // AI 서버 요청 payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sessionId", sessionId);
            payload.put("topic", topic);
            payload.put("inputText", req.getInputText());
            payload.put("imageBase64", req.getImageBase64());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiServerUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("AI Server Error");
            }

            JsonNode root = mapper.readTree(response.getBody());

            // AI 결과 파싱
            String mergedSentence = root.path("mergedSentence").asText("");
            String interpretedSentence = geminiService.interpretMergedSentence(mergedSentence);

            List<String> keywords = mapper.convertValue(
                    root.path("keywords"),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            keywords = geminiService.translateKeywords(keywords);

            List<AiResponseResponse.Recommendation> recs = new ArrayList<>();
            for (JsonNode songNode : root.path("recommendations")) {
                recs.add(AiResponseResponse.Recommendation.builder()
                        .songId(songNode.path("songId").asText(null))
                        .title(songNode.path("title").asText(""))
                        .artist(songNode.path("artist").asText(""))
                        .albumCover(songNode.path("albumCover").asText(""))
                        .previewUrl(songNode.path("previewUrl").asText(""))
                        .build());
            }

            String aiMessage = root.path("aiMessage").asText("AI 추천 결과입니다.");

            // AI 메시지 저장
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSession(session);
            aiMsg.setSender("ai");
            aiMsg.setText(aiMessage);
            aiMsg.setKeywordsJson(mapper.writeValueAsString(keywords));
            aiMsg.setRecommendationsJson(mapper.writeValueAsString(recs));
            aiMsg.setMergedSentence(mergedSentence);
            aiMsg.setInterpretedSentence(interpretedSentence);
            messageRepo.save(aiMsg);

            return AiResponseResponse.builder()
                    .sessionId(sessionId)
                    .topic(topic)
                    .keywords(keywords)
                    .aiMessage(aiMessage)
                    .mergedSentence(mergedSentence)
                    .interpretedSentence(interpretedSentence)
                    .recommendations(recs)
                    .timestamp(LocalDateTime.now())
                    .build();

        } catch (Exception e) {
            e.printStackTrace();
            return AiResponseResponse.builder()
                    .sessionId(req.getSessionId())
                    .topic("추천 생성 실패")
                    .keywords(List.of("error"))
                    .aiMessage("AI 추천 서버와 통신 중 오류가 발생했습니다.")
                    .recommendations(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // =====================================================
    // ④ 세션 전체 메시지 조회 (모든 세션 허용)
    // =====================================================
    public ChatHistoryResponse getChatFullHistory(Long sessionId) {

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // [수정]: 최신 세션 체크 로직 제거 (모든 세션의 전체 기록 조회 허용)
        List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);

        List<ChatMessageResponse> list = new ArrayList<>();
        for (ChatMessage msg : messages) {

            List<String> keywords = new ArrayList<>();
            List<AiResponseResponse.Recommendation> recs = new ArrayList<>();
            try {
                if (msg.getKeywordsJson() != null)
                    keywords = mapper.readValue(msg.getKeywordsJson(), List.class);

                if (msg.getRecommendationsJson() != null)
                    recs = Arrays.asList(
                            mapper.readValue(msg.getRecommendationsJson(),
                                    AiResponseResponse.Recommendation[].class)
                    );
            } catch (Exception ignored) {}

            list.add(ChatMessageResponse.builder()
                    .messageId(msg.getId())
                    .sessionId(sessionId)
                    .sender(msg.getSender())
                    .text(msg.getText())
                    .imageBase64(msg.getImageUrl() != null
                            ? List.of(msg.getImageUrl())
                            : null)
                    .keywords(keywords)
                    .recommendations(recs)
                    // [추가] ChatMessageResponse에 mergedSentence와 interpretedSentence가 있다고 가정하고 매핑
                    .mergedSentence(msg.getMergedSentence())
                    .interpretedSentence(msg.getInterpretedSentence())
                    .timestamp(msg.getCreatedAt())
                    .build());
        }

        return ChatHistoryResponse.builder()
                .sessionId(sessionId)
                .type(session.getType())
                .topic(session.getTopic())
                .isLatest(true) // 이 필드는 클라이언트에서 판단할 수 있도록 true로 유지
                .messages(list)
                .build();
    }

    // =====================================================
    // ⑤ 요약 조회 (활성화되지 않은 세션만 허용)
    // =====================================================
    public ChatSummaryResponse getChatSummary(Long sessionId) {

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 1. 🔥 활성 세션 차단 로직 (최신 세션은 요약 불가)
        Optional<ChatSession> latest =
                sessionRepo.findTopByTypeOrderByStartTimeDesc(session.getType());

        if (latest.isPresent() && Objects.equals(latest.get().getId(), sessionId)) {
            // 활성화된 최신 세션일 경우 차단
            throw new RuntimeException("활성화된 세션 (" + sessionId + ")에 대해서는 채팅 요약을 요청할 수 없습니다. 전체 기록 조회 엔드포인트를 사용해야 합니다.");
        }

        // --- 이전 세션에 대한 요약 생성 로직 시작 (try-catch로 안정화) ---
        try {
            // 2. 메시지 로드
            List<ChatMessage> messages =
                    messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);

            if (messages.isEmpty()) {
                throw new RuntimeException("메시지가 없습니다.");
            }

            // 세션 종료 시간
            // [확인]: isEnded가 true일 때만 endTime을 반환합니다.
            LocalDateTime endTime = (session.getIsEnded() != null && session.getIsEnded())
                    ? session.getEndTime() : null;

            // 대표 user 메시지 (가장 마지막 user 메시지)
            ChatMessage lastUserMsg = messages.stream()
                    .filter(m -> m.getSender().equals("user"))
                    .reduce((a, b) -> b).orElse(null);

            String repText = lastUserMsg != null ? lastUserMsg.getText() : null;
            
            // 🖼️ repImages 정리 (사진 정보 가져오기)
            List<String> repImages = new ArrayList<>();
            if (lastUserMsg != null && lastUserMsg.getImageUrl() != null && !lastUserMsg.getImageUrl().isBlank()) {
                repImages.add(lastUserMsg.getImageUrl());
            }

            // 전체 대화 병합
            String full = messages.stream()
                    .map(ChatMessage::getText)
                    .filter(Objects::nonNull)
                    .reduce((a, b) -> a + "\n" + b)
                    .orElse("");

            // Gemini를 사용한 대화 요약
            String summary = geminiService.summarizeConversation(full);
            String latestUserSummary =
                    repText != null ? geminiService.summarizeSentence(repText) : null;

            // 마지막 AI 메시지
            ChatMessage lastAi = messages.stream()
                    .filter(m -> m.getSender().equals("ai"))
                    .reduce((a, b) -> b).orElse(null);

            List<String> keywords = new ArrayList<>();
            List<AiResponseResponse.Recommendation> recs = new ArrayList<>();
            String merged = null;
            String interpreted = null;

            if (lastAi != null) {
                try {
                    if (lastAi.getKeywordsJson() != null)
                        keywords = mapper.readValue(lastAi.getKeywordsJson(), List.class);

                    if (lastAi.getRecommendationsJson() != null)
                        recs = Arrays.asList(
                                mapper.readValue(lastAi.getRecommendationsJson(),
                                        AiResponseResponse.Recommendation[].class));

                    merged = lastAi.getMergedSentence();
                    interpreted = lastAi.getInterpretedSentence();

                } catch (Exception ignored) {} // 내부 JSON 파싱 오류는 무시
            }

            ChatSummaryResponse.SummaryMode mode =
                    ChatSummaryResponse.SummaryMode.builder()
                            .summary(summary)
                            .keywords(keywords)
                            .recommendations(recs)
                            .mergedSentence(merged)
                            .interpretedSentence(interpreted)
                            .build();

            return ChatSummaryResponse.builder()
                    .sessionId(sessionId)
                    .type(session.getType())
                    .topic(session.getTopic())
                    .isLatest(false)
                    .representativeText(repText)
                    .representativeImages(repImages)
                    .latestUserSummary(latestUserSummary)
                    .summaryMode(mode)
                    // [확인]: 시작 시간과 종료 시간을 모두 반환합니다.
                    .timestamp(session.getStartTime()) 
                    .endTime(endTime)
                    .build();
            
        } catch (Exception e) {
            // 🔥 요약 처리 중 발생하는 모든 예외를 잡아서 안정적인 오류 응답 반환
            e.printStackTrace();
            return ChatSummaryResponse.builder()
                    .sessionId(sessionId)
                    .type(session.getType())
                    .topic("요약 처리 오류")
                    .isLatest(false)
                    .representativeText("요약 데이터 로드 중 오류 발생: " + e.getMessage())
                    .summaryMode(ChatSummaryResponse.SummaryMode.builder()
                            .summary("데이터 처리 중 오류가 발생했습니다.")
                            .build())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // =====================================================
    // ⑥ 후속 메시지 전송 (user → AI 호출)
    // =====================================================
    public ChatMessageResponse sendUserMessage(ChatMessageRequest req) {
        Long sessionId = req.getSessionId();

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 최신 세션인지 체크
        Optional<ChatSession> latest =
                sessionRepo.findTopByTypeOrderByStartTimeDesc(session.getType());

        if (latest.isEmpty() || !Objects.equals(latest.get().getId(), sessionId)) {
            throw new RuntimeException("이전 세션에는 후속 메시지를 보낼 수 없습니다.");
        }

        // ① 사용자 메시지 저장
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender("user");
        userMsg.setText(req.getText());
        if (req.getImageBase64() != null && !req.getImageBase64().isBlank()) {
            userMsg.setImageUrl(req.getImageBase64());
        }
        messageRepo.save(userMsg);

        // ② AI 호출
        AiResponseRequest aiReq = new AiResponseRequest();
        aiReq.setSessionId(sessionId);
        aiReq.setInputText(req.getText());
        aiReq.setImageBase64(req.getImageBase64());

        AiResponseResponse aiRes = generateAiResponse(aiReq);

        // ③ 사용자 메시지 기준 응답 반환
        return ChatMessageResponse.builder()
                .messageId(userMsg.getId())
                .sessionId(sessionId)
                .sender("user")
                .text(req.getText())
                .imageBase64(req.getImageBase64() != null && !req.getImageBase64().isBlank()
                        ? List.of(req.getImageBase64()) : null)
                .keywords(aiRes.getKeywords())
                .recommendations(aiRes.getRecommendations())
                // [수정]: DTO에 필드가 있다고 가정하고 매핑
                .mergedSentence(aiRes.getMergedSentence()) 
                .interpretedSentence(aiRes.getInterpretedSentence()) 
                .timestamp(userMsg.getCreatedAt())
                .build();
    }

    // =====================================================
    // ⑦ 세션 목록 조회 (isEnded, endTime 정보 추가)
    // =====================================================
    public List<ChatSessionSummaryResponse> getSessionList(String type) {
        List<ChatSession> sessions = sessionRepo.findByTypeOrderByStartTimeDesc(type);

        List<ChatSessionSummaryResponse> list = new ArrayList<>();

        for (ChatSession s : sessions) {

            Optional<ChatMessage> lastMsg =
                    messageRepo.findTopBySessionIdOrderByCreatedAtDesc(s.getId());

            String latestMsg = lastMsg.map(ChatMessage::getText).orElse("");

            list.add(ChatSessionSummaryResponse.builder()
                    .sessionId(s.getId())
                    .type(s.getType())
                    .topic(s.getTopic())
                    .latestMessage(latestMsg)
                    // [반영]: 시작 시간
                    .createdAt(s.getStartTime()) 
                    // 🔥 [추가]: isEnded 상태와 종료 시간
                    .isEnded(s.getIsEnded() != null && s.getIsEnded())
                    .endTime(s.getEndTime()) 
                    .build());
        }

        return list;
    }

    public List<ChatSessionSummaryResponse> getMySessionList() {
        return getSessionList("MY");
    }

    public List<ChatSessionSummaryResponse> getSpaceSessionList() {
        return getSessionList("SPACE");
    }
}