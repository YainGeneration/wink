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
    private String aiServerUrl; // ex) http://127.0.0.1:5001/api/recommend

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

        // Gemini 기반 주제 추출
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

    // ✅ Flask AI 서버와 통신 (Agent3 통합 파이프라인)
    public AiResponseResponse generateAiResponse(AiResponseRequest req) {
        try {
            Long sessionId = req.getSessionId();
            ChatSession session = sessionRepo.findById(sessionId)
                    .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

            String topic = session.getTopic();

            // Flask 요청 payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("sessionId", sessionId);
            payload.put("topic", topic);
            payload.put("inputText", req.getInputText());
            payload.put("imageUrls", req.getImageUrls());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            System.out.println("🌐 Flask AI 서버 요청 전송 → " + aiServerUrl);
            ResponseEntity<String> response = restTemplate.exchange(
                    aiServerUrl, HttpMethod.POST, entity, String.class
            );

            // 사용자 메시지 저장
            ChatMessage userMsg = new ChatMessage();
            userMsg.setSession(session);
            userMsg.setSender("user");
            userMsg.setText(req.getInputText());
            if (req.getImageUrls() != null && !req.getImageUrls().isEmpty()) {
                userMsg.setImageUrl(String.join(",", req.getImageUrls()));
            }
            messageRepo.save(userMsg);

            // 응답 처리
            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode root = mapper.readTree(response.getBody());

                List<String> keywords = mapper.convertValue(
                        root.path("keywords"),
                        mapper.getTypeFactory().constructCollectionType(List.class, String.class)
                );

                List<AiResponseResponse.Recommendation> recs = new ArrayList<>();
                for (JsonNode song : root.path("recommendations")) {
                    recs.add(AiResponseResponse.Recommendation.builder()
                            .title(song.path("title").asText())
                            .artist(song.path("artist").asText())
                            .albumCover(song.path("albumCover").asText(null))
                            .previewUrl(song.path("previewUrl").asText(null))
                            .build());
                }

                String aiMessage = root.path("aiMessage").asText("AI 추천 결과입니다.");
                String mergedSentence = root.path("mergedSentence").asText("");

                // AI 메시지 저장
                ChatMessage aiMsg = new ChatMessage();
                aiMsg.setSession(session);
                aiMsg.setSender("ai");
                aiMsg.setText(aiMessage + "\n" + mergedSentence);
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
                    .aiMessage("AI 서버와 통신 중 오류가 발생했습니다.")
                    .recommendations(List.of())
                    .timestamp(LocalDateTime.now())
                    .build();
        }
    }

    // ✅ 기존 대화 이력 조회 (그대로 유지)
    public ChatHistoryResponse getMyChatHistory(Long sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        List<ChatMessageResponse> messageResponses = new ArrayList<>();

        ObjectMapper mapper = new ObjectMapper();

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
}
