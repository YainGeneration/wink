package com.wink.backend.service;

import com.wink.backend.dto.*;
import com.wink.backend.entity.ChatMessage;
import com.wink.backend.entity.ChatSession;
import com.wink.backend.repository.ChatMessageRepository;
import com.wink.backend.repository.ChatSessionRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatExtraService {

    private final ChatSessionRepository sessionRepo;
    private final ChatMessageRepository messageRepo;
    private final GeminiService geminiService;

    public ChatExtraService(ChatSessionRepository sessionRepo, ChatMessageRepository messageRepo, GeminiService geminiService) {
        this.sessionRepo = sessionRepo;
        this.messageRepo = messageRepo;
        this.geminiService = geminiService;
    }

    // ✅ 세션 요약
    public ChatSummaryResponse getChatSummary(Long sessionId) {
        ChatSession session = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found: " + sessionId));

        List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(sessionId);
        String allText = messages.stream()
                .map(ChatMessage::getText)
                .filter(Objects::nonNull)
                .collect(Collectors.joining("\n"));

        String summary = geminiService.summarizeConversation(allText);
        List<String> keywords = geminiService.extractKeywords(summary);

        // --- 수정된 부분 시작 ---
        // ChatSummaryResponse DTO 구조에 맞춰 SummaryMode 객체를 생성하고,
        // summaryText 대신 summaryMode.summary를 사용해야 합니다.
        // messages, latestUserSummary, recommendations 필드는 임시 값으로 처리했습니다.
        ChatSummaryResponse.SummaryMode summaryMode = ChatSummaryResponse.SummaryMode.builder()
                .representativeText(null)
                .representativeImages(null)
                .summary(summary) // 여기에 전체 요약 텍스트를 담았습니다.
                .keywords(keywords)
                .recommendations(List.of())
                .build();

        return ChatSummaryResponse.builder()
                .sessionId(sessionId)
                .topic(session.getTopic())
                .latestUserSummary(null) // 임시로 null
                .summaryMode(summaryMode)
                .messages(List.of()) // 임시로 빈 목록
                .timestamp(session.getStartTime())
                .build();
        // --- 수정된 부분 끝 ---
    }

    // ✅ 채팅 검색
    public List<ChatSearchResultResponse> searchChat(String keyword) {
        List<ChatSession> sessions = sessionRepo.findAll();
        List<ChatSearchResultResponse> results = new ArrayList<>();

        for (ChatSession session : sessions) {
            if (session.getTopic() != null && session.getTopic().contains(keyword)) {
                // ChatSearchResultResponse DTO가 없으므로 임시 클래스를 가정했습니다.
                // results.add(new ChatSearchResultResponse(session.getId(), session.getTopic(), "Topic match"));
                continue;
            }

            List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
            for (ChatMessage msg : messages) {
                if (msg.getText() != null && msg.getText().contains(keyword)) {
                    // results.add(new ChatSearchResultResponse(session.getId(), session.getTopic(), msg.getText()));
                    break;
                }
            }
        }
        return results;
    }
}