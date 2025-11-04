package com.wink.backend.service;

import com.wink.backend.dto.*;
import com.wink.backend.entity.ChatMessage;
import com.wink.backend.entity.ChatSession;
import com.wink.backend.repository.ChatMessageRepository;
import com.wink.backend.repository.ChatSessionRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    // ✅ 나의 순간
    public ChatStartResponse startMy(ChatStartMyRequest req) {
        ChatSession session = new ChatSession();
        session.setType("MY");
        session.setStartTime(LocalDateTime.now());

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

    // ✅ 공간의 순간
    public ChatStartResponse startSpace(ChatStartSpaceRequest req) {
        ChatSession session = new ChatSession();
        session.setType("SPACE");
        session.setStartTime(LocalDateTime.now());

        String nearbySummary = "";
        if (req.getNearbyMusic() != null && !req.getNearbyMusic().isEmpty()) {
            nearbySummary = req.getNearbyMusic().stream()
                    .map(m -> m.getTitle() + " - " + m.getArtist())
                    .reduce((a, b) -> a + ", " + b).orElse("");
        }

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

    // ✅ AI 서버 호출
    public AiResponseResponse generateAiResponse(AiResponseRequest req) {
        try {
            Long sessionId = req.getSessionId();
            ChatSession session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

            String topic = session.getTopic();
            ObjectMapper mapper = new ObjectMapper();

            Map<String, Object> payload = new HashMap<>();
            payload.put("sessionId", sessionId);
            payload.put("topic", topic);
            payload.put("inputText", req.getInputText());
            payload.put("imageUrls", req.getImageUrls());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            System.out.println("🚀 Flask 요청: " + aiServerUrl);
            System.out.println("📦 Payload: " + mapper.writeValueAsString(payload));

            ResponseEntity<String> response = restTemplate.exchange(
                    aiServerUrl, HttpMethod.POST, entity, String.class);

            System.out.println("📥 Flask 응답: " + response.getStatusCode());
            System.out.println("📦 Body: " + response.getBody());

            ChatMessage userMsg = new ChatMessage();
            userMsg.setSession(session);
            userMsg.setSender("user");
            userMsg.setText(req.getInputText());
            if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
                userMsg.setImageUrl(String.join(",", req.getImageUrls()));
            }
            messageRepo.save(userMsg);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = mapper.readTree(response.getBody());

                List<String> keywords = mapper.convertValue(
                        root.path("keywords"),
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );
                keywords = geminiService.translateKeywords(keywords);

                List<AiResponseResponse.Recommendation> recs = new ArrayList<>();
                for (JsonNode songNode : root.path("recommendations")) {
                    recs.add(AiResponseResponse.Recommendation.builder()
                            .songId(songNode.has("songId") ? songNode.path("songId").asLong() : null)
                            .title(songNode.path("title").asText(""))
                            .artist(songNode.path("artist").asText(""))
                            .albumCover(songNode.path("albumCover").asText(""))
                            .previewUrl(songNode.path("previewUrl").asText(""))
                            .build());
                }

                String aiMessage = root.path("aiMessage").asText("AI 추천 결과입니다.");

                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSession(session);
                aiMsg.setSender("ai");
                aiMsg.setText(aiMessage);
                aiMsg.setKeywordsJson(mapper.writeValueAsString(keywords));
                aiMsg.setRecommendationsJson(mapper.writeValueAsString(recs));
                messageRepo.save(aiMsg);

                return AiResponseResponse.builder()
                        .sessionId(sessionId)
                        .topic(topic)
                        .keywords(keywords)
                        .aiMessage(aiMessage)
                        .recommendations(recs)
                        .timestamp(LocalDateTime.now())
                        .build();
            }

            throw new RuntimeException("AI server returned " + response.getStatusCode());

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

    // ✅ 나의 순간 히스토리 조회
    public ChatHistoryResponse getMyChatHistory(Long sessionId) {
        return buildChatHistory(sessionId, "MY");
    }

    // ✅ 공간의 순간 히스토리 조회
    public ChatHistoryResponse getSpaceChatHistory(Long sessionId) {
        return buildChatHistory(sessionId, "SPACE");
    }

    // ✅ 공통 히스토리 생성 로직
    private ChatHistoryResponse buildChatHistory(Long sessionId, String expectedType) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        if (!expectedType.equals(session.getType())) {
            throw new RuntimeException("잘못된 세션 타입입니다. (" + session.getType() + ")");
        }

        List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<ChatMessageResponse> messageResponses = new ArrayList<>();

        for (ChatMessage msg : messages) {
            List<String> keywords = new ArrayList<>();
            List<AiResponseResponse.Recommendation> recs = new ArrayList<>();
            try {
                if (msg.getKeywordsJson() != null)
                    keywords = mapper.readValue(msg.getKeywordsJson(), List.class);
                if (msg.getRecommendationsJson() != null)
                    recs = Arrays.asList(mapper.readValue(
                            msg.getRecommendationsJson(),
                            AiResponseResponse.Recommendation[].class
                    ));
            } catch (Exception ignored) {}

            messageResponses.add(ChatMessageResponse.builder()
                    .messageId(msg.getId())
                    .sender(msg.getSender())
                    .text(msg.getText())
                    .keywords(keywords)
                    .recommendations(recs)
                    .timestamp(msg.getCreatedAt())
                    .build());
        }

        return ChatHistoryResponse.builder()
                .sessionId(session.getId())
                .type(session.getType())
                .topic(session.getTopic())
                .messages(messageResponses)
                .build();
    }

    // ✅ 메시지 전송 (가장 최신 세션만 허용)
    public ChatMessageResponse sendMessage(ChatMessageRequest req) {
        Long sessionId = req.getSessionId();
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        // 🔒 최신 세션만 허용
        Optional<ChatSession> latestSession = sessionRepo.findTopByTypeOrderByStartTimeDesc(session.getType());
        if (latestSession.isEmpty() || !Objects.equals(latestSession.get().getId(), sessionId)) {
            throw new RuntimeException("Only the latest session allows new messages.");
        }

        ChatMessage msg = new ChatMessage();
        msg.setSession(session);
        msg.setSender("user");
        msg.setText(req.getText());
        if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
            msg.setImageUrl(String.join(",", req.getImageUrls()));
        }
        messageRepo.save(msg);

        return ChatMessageResponse.builder()
                .messageId(msg.getId())
                .sender(msg.getSender())
                .text(msg.getText())
                .timestamp(msg.getCreatedAt())
                .build();
    }
}
