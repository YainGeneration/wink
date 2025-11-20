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
    // ① 나의 순간 시작
    // =====================================================
    public ChatStartResponse startMy(ChatStartMyRequest req) {

        endPreviousSessions("MY"); // 같은 타입 모두 종료

        ChatSession session = new ChatSession();
        session.setType("MY");
        session.setStartTime(LocalDateTime.now());
        session.setIsEnded(false);

        // 🔥 제미나이 기반 주제 생성 (너가 만든 로직 그대로)
        String topic = geminiService.extractTopic(req.getInputText());
        session.setTopic(topic);

        sessionRepo.save(session);

        return new ChatStartResponse(
                session.getId(),
                session.getType(),
                session.getTopic(),
                "Gemini 기반 주제 추출 완료",
                session.getStartTime()
        );
    }

    // =====================================================
    // ② 공간의 순간 시작
    // =====================================================
    public ChatStartResponse startSpace(ChatStartSpaceRequest req) {

        endPreviousSessions("SPACE"); // 같은 타입 모두 종료

        ChatSession session = new ChatSession();
        session.setType("SPACE");
        session.setStartTime(LocalDateTime.now());
        session.setIsEnded(false);

        // 주변 음악 요약
        String nearbySummary = "";
        if (req.getNearbyMusic() != null && !req.getNearbyMusic().isEmpty()) {
            nearbySummary = req.getNearbyMusic().stream()
                    .map(m -> m.getTitle() + " - " + m.getArtist())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        // 🔥 네가 만든 장소 기반 프롬프트 그대로 적용
        String prompt = String.format(
                "📍장소명: %s (%s)\n🎧 주변 음악: %s\n이 장소의 분위기와 어울리는 음악적 주제를 한 문장으로 요약해줘.",
                req.getLocation().getPlaceName(),
                req.getLocation().getAddress(),
                nearbySummary.isBlank() ? "정보 없음" : nearbySummary
        );

        String topic = geminiService.extractTopic(prompt);
        session.setTopic(topic);

        sessionRepo.save(session);

        return new ChatStartResponse(
                session.getId(),
                session.getType(),
                session.getTopic(),
                "Gemini 기반 공간 주제 생성 완료",
                session.getStartTime()
        );
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
    // ④ 최신 세션 전체 메시지 조회
    // =====================================================
    public ChatHistoryResponse getChatFullHistory(Long sessionId) {

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 최신 세션인지 체크
        Optional<ChatSession> latest =
                sessionRepo.findTopByTypeOrderByStartTimeDesc(session.getType());

        if (latest.isEmpty() || !Objects.equals(latest.get().getId(), sessionId)) {
            throw new RuntimeException("최신 세션만 전체 대화 조회 가능합니다.");
        }

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
                    .timestamp(msg.getCreatedAt())
                    .build());
        }

        return ChatHistoryResponse.builder()
                .sessionId(sessionId)
                .type(session.getType())
                .topic(session.getTopic())
                .isLatest(true)
                .messages(list)
                .build();
    }

    // =====================================================
    // ⑤ 요약 조회 (최신 세션 제외)
    // =====================================================
    public ChatSummaryResponse getChatSummary(Long sessionId) {

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        Optional<ChatSession> latest =
                sessionRepo.findTopByTypeOrderByStartTimeDesc(session.getType());

        if (latest.isPresent() && Objects.equals(latest.get().getId(), sessionId)) {
            throw new RuntimeException("최신 세션은 요약 페이지를 사용할 수 없습니다.");
        }

        List<ChatMessage> messages =
                messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);

        if (messages.isEmpty()) {
            throw new RuntimeException("메시지가 없습니다.");
        }

        // 대표 user 메시지
        ChatMessage lastUserMsg = messages.stream()
                .filter(m -> m.getSender().equals("user"))
                .reduce((a, b) -> b).orElse(null);

        String repText = lastUserMsg != null ? lastUserMsg.getText() : null;
        List<String> repImages =
                (lastUserMsg != null && lastUserMsg.getImageUrl() != null)
                        ? List.of(lastUserMsg.getImageUrl())
                        : null;

        // 전체 대화 요약
        String full = messages.stream()
                .map(ChatMessage::getText)
                .filter(Objects::nonNull)
                .reduce((a, b) -> a + "\n" + b)
                .orElse("");

        String summary = geminiService.summarizeConversation(full);
        String latestUserSummary =
                repText != null ? geminiService.summarizeSentence(repText) : null;

        // 마지막 AI 메시지 정보
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

            } catch (Exception ignored) {}
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
                .timestamp(session.getStartTime())
                .build();
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
                .timestamp(userMsg.getCreatedAt())
                .build();
    }

    // =====================================================
    // ⑦ 세션 목록 조회
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
                    .createdAt(s.getStartTime())
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
