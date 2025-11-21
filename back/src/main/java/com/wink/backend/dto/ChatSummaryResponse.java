package com.wink.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSummaryResponse {

    private Long sessionId;
    private String type;
    private String topic;
    private boolean isLatest;

    // 대표 user 메시지 / 이미지
    private String representativeText;
    private List<String> representativeImages;

    // 최신 user 한 줄 요약 (Gemini)
    private String latestUserSummary;

    // 요약/키워드/추천/문장 묶음
    private SummaryMode summaryMode;

    // 대화 시작 시간
    private LocalDateTime timestamp;
    
    // 🔥 [추가]: 대화 종료 시간
    private LocalDateTime endTime; 

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryMode {
        private String summary;
        private List<String> keywords;
        private List<AiResponseResponse.Recommendation> recommendations;
        private String mergedSentence;
        private String interpretedSentence;
        private String englishText;
        private String englishCaption;
        private String imageDescriptionKo;
    }
}