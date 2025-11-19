package com.wink.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class AiResponseResponse {

    private Long sessionId;
    private String topic;
    private List<String> keywords;
    private String aiMessage;
    private List<Recommendation> recommendations;
    private LocalDateTime timestamp;

    /**
     * AI 추천 노래 정보
     */
    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Recommendation {
        private Long songId;
        private String title;
        private String artist;
        private String albumCover;
        private String previewUrl;
    }
}