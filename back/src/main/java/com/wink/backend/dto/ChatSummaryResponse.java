package com.wink.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class ChatSummaryResponse {

    private Long sessionId;
    private String topic;

    // 최신 사용자 메시지 요약
    private String latestUserSummary;

    // 요약 모드 전체 블록
    private SummaryMode summaryMode;

    // 전체 메시지 목록
    private List<ChatMessageResponse> messages;

    private LocalDateTime timestamp;

    // =========================
    // 🎯 내부 클래스 추가
    // =========================
    @Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
    public static class SummaryMode { 
        private String representativeText;
        private List<String> representativeImages;
        private String summary;
        private List<String> keywords;
        private List<AiResponseResponse.Recommendation> recommendations;
    }
}
