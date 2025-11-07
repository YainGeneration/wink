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

        return ChatSummaryResponse.builder()
                .sessionId(sessionId)
                .topic(session.getTopic())
                .summaryText(summary)
                .keywords(keywords)
                .recommendations(List.of()) // 추천은 나중에 연동
                .build();
    }

    // ✅ 채팅 검색
    public List<ChatSearchResultResponse> searchChat(String keyword) {
        List<ChatSession> sessions = sessionRepo.findAll();
        List<ChatSearchResultResponse> results = new ArrayList<>();

        for (ChatSession session : sessions) {
            if (session.getTopic() != null && session.getTopic().contains(keyword)) {
                results.add(new ChatSearchResultResponse(session.getId(), session.getTopic(), "Topic match"));
                continue;
            }

            List<ChatMessage> messages = messageRepo.findBySessionIdOrderByCreatedAtAsc(session.getId());
            for (ChatMessage msg : messages) {
                if (msg.getText() != null && msg.getText().contains(keyword)) {
                    results.add(new ChatSearchResultResponse(session.getId(), session.getTopic(), msg.getText()));
                    break;
                }
            }
        }
        return results;
    }
}
