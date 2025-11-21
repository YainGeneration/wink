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
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class ChatService {

    private final ChatSessionRepository sessionRepo;
    private final GeminiService geminiService;
    private final ChatMessageRepository messageRepo;
    private final RestTemplate restTemplate;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ImageService imageService;
    private final LocationService locationService;


    @Value("${ai.server.url}")
    private String aiServerUrl;

    public ChatService(ChatSessionRepository sessionRepo,
                    GeminiService geminiService,
                    ChatMessageRepository messageRepo,
                    ImageService imageService,
                    LocationService locationService) {
        this.sessionRepo = sessionRepo;
        this.geminiService = geminiService;
        this.messageRepo = messageRepo;
        this.imageService = imageService;
        this.locationService = locationService;
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
    public ChatHistoryResponse startMy(ChatStartMyRequest req) {

        endPreviousSessions("MY"); // 같은 타입 모두 종료

        ChatSession session = new ChatSession();
        session.setType("MY");
        session.setStartTime(LocalDateTime.now());
        session.setIsEnded(false);

        // 🔥 제미나이 기반 주제 생성
        String topic = geminiService.extractTopic(req.getInputText());
        session.setTopic(topic);

        sessionRepo.save(session); // 세션 저장

        // 1. 첫 사용자 메시지 저장 (텍스트 + 이미지)
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender("user");
        userMsg.setText(req.getInputText());
        if (req.getImageBase64() != null && !req.getImageBase64().isBlank()) {
            try {
                String fileName = imageService.saveBase64Image(req.getImageBase64());
                userMsg.setImageUrl(fileName);
            } catch (IOException e) {
                e.printStackTrace();
                userMsg.setImageUrl(null);
            }
        }
        messageRepo.save(userMsg); // 채팅 내용 저장 완료

        // 2. AI 응답 생성 요청 (이미지도 함께 전달)
        AiResponseRequest aiReq = new AiResponseRequest();
        aiReq.setSessionId(session.getId());
        aiReq.setInputText(req.getInputText());
        aiReq.setImageBase64(req.getImageBase64());
        aiReq.setLocation(null);       // MY에는 절대 보내지 않음
        aiReq.setNearbyMusic(null);

        // 3. AI 응답을 받아 바로 반환
        generateAiResponse(aiReq);
        return getChatFullHistory(session.getId());
    }

    // =====================================================
    // ② 공간의 순간 시작 (→ 사용자 메시지 저장 + 바로 AI 응답 생성)
    // =====================================================
    // 반환 타입: AiResponseResponse
    public ChatHistoryResponse startSpace(ChatStartSpaceRequest req) {

        endPreviousSessions("SPACE"); // 같은 타입 모두 종료

        ChatSession session = new ChatSession();
        session.setType("SPACE");
        session.setStartTime(LocalDateTime.now());
        session.setIsEnded(false);

        // ★ 위치 저장 (후속 메시지에서 사용할 lat/lng)
        session.setStartLat(req.getLocation().getLat());
        session.setStartLng(req.getLocation().getLng());

        // 주변 음악 요약 텍스트
        String nearbySummary = "";
        if (req.getNearbyMusic() != null && !req.getNearbyMusic().isEmpty()) {
            nearbySummary = req.getNearbyMusic().stream()
                    .map(m -> m.getTitle() + " - " + m.getArtist())
                    .reduce((a, b) -> a + ", " + b)
                    .orElse("");
        }

        String prompt = String.format(
                "📍장소명: %s (%s)\n🎧 주변 음악: %s\n이 장소의 분위기를 요약해줘.",
                req.getLocation().getPlaceName(),
                req.getLocation().getAddress(),
                nearbySummary.isBlank() ? "정보 없음" : nearbySummary
        );

        // String topic = geminiService.extractTopic(prompt);
        // session.setTopic(topic);
        // sessionRepo.save(session);
        String topic;
        try {
            topic = geminiService.extractTopic(prompt);

            // 혹시라도 응답이 빈 문자열이면 대체
            if (topic == null || topic.isBlank()) {
                topic = "오늘의 공간 감성 음악";
            }
        } catch (Exception e) {
            topic = "오늘의 공간 감성 음악";
        }

        session.setTopic(topic);
        sessionRepo.save(session);


        // 첫 메시지 저장
        String initialText = req.getLocation().getPlaceName() + "에 왔습니다.";
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSession(session);
        userMsg.setSender("user");
        userMsg.setText(initialText);
        messageRepo.save(userMsg);

        // ----- AI 요청 -----
        AiResponseRequest aiReq = new AiResponseRequest();
        aiReq.setSessionId(session.getId());
        aiReq.setInputText(initialText);
        aiReq.setImageBase64(req.getImageBase64());

        // aiReq.setLocation(req.getLocation());
        // // ★ 고정 5개 주변 음악 넣기
        // List<NearbyMusicResponse> fixedList =
        //         locationService.getNearbyMusic(req.getLocation().getLat(), req.getLocation().getLng());

        // List<ChatStartSpaceRequest.NearbyMusic> fixedMusic = fixedList.stream()
        //         .map(m -> {
        //             ChatStartSpaceRequest.NearbyMusic nm = new ChatStartSpaceRequest.NearbyMusic();
        //             nm.setSongId(null);
        //             nm.setTitle(m.getSongTitle());
        //             nm.setArtist(m.getArtist());
        //             return nm;
        //         })
        //         .toList();
        // aiReq.setNearbyMusic(fixedMusic);
        // ---------- 고정 Location ----------
        ChatStartSpaceRequest.Location fixedLoc = new ChatStartSpaceRequest.Location();
        fixedLoc.setLat(37.545900);
        fixedLoc.setLng(126.964400);
        fixedLoc.setAddress("서울특별시 용산구 청파동");
        fixedLoc.setPlaceName("숙명여자대학교 정문");

        aiReq.setLocation(fixedLoc);

        // ---------- 고정 주변 음악 ----------
        aiReq.setNearbyMusic(locationService.getFixedNearbyMusic());


        // 3. AI 응답 생성
        generateAiResponse(aiReq);

        // 4. 전체 메시지 구조(ChatHistoryResponse)로 반환
        return getChatFullHistory(session.getId());    
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
            if ("SPACE".equals(session.getType())) {
                payload.put("location", req.getLocation());
                payload.put("nearbyMusic", req.getNearbyMusic());
            } else {
                // 🔥 MY 타입에서는 아예 보내지 않음
                payload.put("location", null);
                payload.put("nearbyMusic", null);
            }


            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    aiServerUrl, HttpMethod.POST, entity, String.class);

            if (response.getStatusCode() != HttpStatus.OK || response.getBody() == null) {
                throw new RuntimeException("AI Server Error");
            }

            JsonNode root = mapper.readTree(response.getBody());

            // -----------------------------
            // 1) 텍스트 관련 필드 파싱
            // -----------------------------
            String mergedSentence = root.path("mergedSentence").asText("");
            String interpretedSentence = geminiService.interpretMergedSentence(mergedSentence);

            // 새로 추가: english_text / english_caption
            String englishText = root.path("englishText").asText(null);
            String englishCaption = root.path("englishCaption").asText(null);

            // 이미지 설명 한국어 버전 (최우선: AI가 직접 준 imageDescriptionKo, 없으면 Gemini 번역)
            String imageDescriptionKo = null;
            if (root.hasNonNull("imageDescriptionKo")
                    && !root.path("imageDescriptionKo").asText("").isBlank()) {
                imageDescriptionKo = root.path("imageDescriptionKo").asText();
            } else if (englishCaption != null && !englishCaption.isBlank()) {
                try {
                    // GeminiService에 translateToKorean(String text) 메서드가 있다고 가정
                    imageDescriptionKo = geminiService.translateToKorean(englishCaption);
                } catch (Exception e) {
                    // 번역 실패 시에도 전체 흐름이 죽지 않도록 로그만 찍고 null 유지
                    e.printStackTrace();
                }
            }

            // 키워드 파싱 + 한국어 번역
            List<String> keywords = mapper.convertValue(
                    root.path("keywords"),
                    mapper.getTypeFactory().constructCollectionType(List.class, String.class)
            );
            keywords = geminiService.translateKeywords(keywords);

            // -----------------------------
            // 2) 추천곡 목록 파싱
            // -----------------------------
            List<AiResponseResponse.Recommendation> recs = new ArrayList<>();

            for (JsonNode songNode : root.path("recommendations")) {

                long durationMs = songNode.path("durationMs").asLong(0);

                long durationSec = 0;               // ★ 먼저 선언
                String durationFormatted = null;    // ★ 먼저 선언

                if (durationMs > 0) {
                    durationSec = durationMs / 1000;   // ms → sec
                    long minutes = durationSec / 60;
                    long seconds = durationSec % 60;
                    durationFormatted = String.format("%02d분 %02d초", minutes, seconds);
                }

                recs.add(AiResponseResponse.Recommendation.builder()
                    .songId(songNode.path("songId").asText(null))
                    .title(songNode.path("title").asText(""))
                    .artist(songNode.path("artist").asText(""))
                    .albumCover(songNode.path("albumCover").asText(""))
                    .previewUrl(songNode.path("previewUrl").asText(""))

                    .durationMs(durationMs)             // ★ 원래 ms 그대로 저장
                    .durationFormatted(durationFormatted)  // ★ 변환된 포맷 저장

                    .trackUrl(songNode.path("trackUrl").asText(null))
                    .spotifyEmbedUrl(songNode.path("spotify_embed_url").asText(null))
                    .build()
                );
            }

            String aiMessage = root.path("aiMessage").asText("AI 추천 결과입니다.");

            // -----------------------------
            // 3) AI 메시지 DB에 저장
            // -----------------------------
            ChatMessage aiMsg = new ChatMessage();
            aiMsg.setSession(session);
            aiMsg.setSender("ai");
            aiMsg.setText(aiMessage);
            aiMsg.setKeywordsJson(mapper.writeValueAsString(keywords));
            aiMsg.setRecommendationsJson(mapper.writeValueAsString(recs));
            aiMsg.setMergedSentence(mergedSentence);
            aiMsg.setInterpretedSentence(interpretedSentence);
            aiMsg.setEnglishText(englishText);
            aiMsg.setEnglishCaption(englishCaption);
            aiMsg.setImageDescriptionKo(imageDescriptionKo);
            messageRepo.save(aiMsg);

            // -----------------------------
            // 4) 프론트로 반환할 응답 생성
            // -----------------------------
            AiResponseResponse.AiResponseResponseBuilder builder =
                    AiResponseResponse.builder()
                            .sessionId(sessionId)
                            .topic(topic)
                            .inputText(req.getInputText())
                            .imageBase64(req.getImageBase64())
                            .keywords(keywords)
                            .aiMessage(aiMessage)
                            .mergedSentence(mergedSentence)
                            .interpretedSentence(interpretedSentence)
                            .englishText(englishText)
                            .englishCaption(englishCaption)
                            .imageDescriptionKo(imageDescriptionKo)
                            .recommendations(recs)
                            .timestamp(LocalDateTime.now());

            // ★ SPACE일 때만 location / nearbyMusic 추가
            if ("SPACE".equals(session.getType())) {

                ChatStartSpaceRequest.Location loc = req.getLocation();

                // LocationResponse로 변환
                LocationResponse converted =
                        new LocationResponse(
                                loc.getPlaceName(),
                                loc.getLat(),
                                loc.getLng(),
                                loc.getAddress()
                        );

                builder.location(converted);
                builder.nearbyMusic(req.getNearbyMusic());
            }

            return builder.build();



        } catch (Exception e) {
            e.printStackTrace();
            return AiResponseResponse.builder()
                    .sessionId(req.getSessionId())
                    .topic("추천 생성 실패")
                    .keywords(List.of("error"))
                    .aiMessage("AI 추천 서버와 통신 중 오류가 발생했습니다.")
                    .mergedSentence(null)
                    .interpretedSentence(null)
                    .englishText(null)
                    .englishCaption(null)
                    .imageDescriptionKo(null)
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
                            ? List.of("http://localhost:8080/chat-images/" + msg.getImageUrl())
                            : null
                        )
                    .keywords(keywords)
                    .recommendations(recs)
                    // [추가] ChatMessageResponse에 mergedSentence와 interpretedSentence가 있다고 가정하고 매핑
                    .mergedSentence(msg.getMergedSentence())
                    .interpretedSentence(msg.getInterpretedSentence())
                    .englishText(msg.getEnglishText())
                    .englishCaption(msg.getEnglishCaption())
                    .imageDescriptionKo(msg.getImageDescriptionKo())
                    
                    .timestamp(msg.getCreatedAt())
                    .build());
        }

        return ChatHistoryResponse.builder()
                .sessionId(sessionId)
                .type(session.getType())
                .topic(session.getTopic())
                .latest(true) // 이 필드는 클라이언트에서 판단할 수 있도록 true로 유지
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
            LocalDateTime endTime = (session.getIsEnded() != null && session.getIsEnded())
                    ? session.getEndTime() : null;

            // 대표 user 메시지 (가장 마지막 user 메시지)
            ChatMessage lastUserMsg = messages.stream()
                    .filter(m -> m.getSender().equals("user"))
                    .reduce((a, b) -> b).orElse(null);

            String repText = lastUserMsg != null ? lastUserMsg.getText() : null;

            // 🖼️ repImages 정리 (사진 정보 가져오기)
            List<String> repImages = new ArrayList<>();

            if (lastUserMsg != null) {
                String fileName = lastUserMsg.getImageUrl();
                if (fileName != null && !fileName.isBlank()) {
                    repImages.add("http://localhost:8080/chat-images/" + fileName);
                }
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
                    .reduce((a, b) -> b)
                    .orElse(null);

            String englishText = lastAi != null ? lastAi.getEnglishText() : null;
            String englishCaption = lastAi != null ? lastAi.getEnglishCaption() : null;
            String imageDescriptionKo = lastAi != null ? lastAi.getImageDescriptionKo() : null;
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
                            .englishText(englishText)
                            .englishCaption(englishCaption)
                            .imageDescriptionKo(imageDescriptionKo)
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
    public ChatHistoryResponse sendUserMessage(ChatMessageRequest req) {

        Long sessionId = req.getSessionId();

        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        // 최신 세션인지 확인
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
            try {
                String fileName = imageService.saveBase64Image(req.getImageBase64());
                userMsg.setImageUrl(fileName);
            } catch (IOException e) {
                userMsg.setImageUrl(null);
            }
        }

        messageRepo.save(userMsg);

        // ② AI 요청 생성
        AiResponseRequest aiReq = new AiResponseRequest();
        aiReq.setSessionId(sessionId);
        aiReq.setInputText(req.getText());
        aiReq.setImageBase64(req.getImageBase64());

        // 🔥 SPACE 후속 메시지에는 location이 request에 없음
        // 그러므로 세션에 저장해둔 startLat/startLng 사용해야 함.
        if ("SPACE".equals(session.getType())) {

            // Location = 세션에 저장된 위치 복원
            ChatStartSpaceRequest.Location loc = new ChatStartSpaceRequest.Location();
            loc.setPlaceName(null);
            loc.setAddress(null);
            loc.setLat(session.getStartLat());
            loc.setLng(session.getStartLng());

            aiReq.setLocation(loc);

            // 고정 주변 음악 리스트 생성
            // List<NearbyMusicResponse> fixedList =
            //         locationService.getNearbyMusic(session.getStartLat(), session.getStartLng());

            // List<ChatStartSpaceRequest.NearbyMusic> fixedMusic = fixedList.stream()
            //         .map(m -> {
            //             ChatStartSpaceRequest.NearbyMusic nm = new ChatStartSpaceRequest.NearbyMusic();
            //             nm.setSongId(null);
            //             nm.setTitle(m.getSongTitle());
            //             nm.setArtist(m.getArtist());
            //             return nm;
            //         })
            //         .toList();

            // aiReq.setNearbyMusic(fixedMusic);
            // ---------- 고정 Location ----------
            ChatStartSpaceRequest.Location fixedLoc = new ChatStartSpaceRequest.Location();
            fixedLoc.setLat(37.545900);
            fixedLoc.setLng(126.964400);
            fixedLoc.setAddress("서울특별시 용산구 청파동");
            fixedLoc.setPlaceName("숙명여자대학교 정문");

            aiReq.setLocation(fixedLoc);

            // ---------- 고정 주변 음악 ----------
            aiReq.setNearbyMusic(locationService.getFixedNearbyMusic());

        } 
        else {
            // MY 유형 → location / nearbyMusic 절대 보내지 않음
            aiReq.setLocation(null);
            aiReq.setNearbyMusic(null);
        }

        // ③ AI 응답 생성 (저장까지 끝)
        generateAiResponse(aiReq);

        // ④ 전체 메시지 구조로 반환
        return getChatFullHistory(sessionId);
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
                    .timestamp(s.getStartTime())
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

    public List<ChatSearchResponse> search(String keyword) {

        List<ChatMessage> messages = messageRepo.findByTextContainingIgnoreCase(keyword);

        // sessionId 기준 중복 제거
        Map<Long, ChatMessage> latestMatch = new LinkedHashMap<>();

        for (ChatMessage msg : messages) {
            Long sessionId = msg.getSession().getId();
            if (!latestMatch.containsKey(sessionId)) {
                latestMatch.put(sessionId, msg);
            }
        }

        List<ChatSearchResponse> result = new ArrayList<>();

        for (ChatMessage msg : latestMatch.values()) {
            ChatSession s = msg.getSession();

            result.add(ChatSearchResponse.builder()
                    .sessionId(s.getId())
                    .topic(s.getTopic())
                    .type(s.getType())
                    .matchedMessage(msg.getText())
                    .timestamp(s.getStartTime())   // ★ 통일된 필드
                    .build()
            );
        }

        return result;
    }

}
