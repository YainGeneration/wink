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
    private String mergedSentence;   // ★ 추가
    private String interpretedSentence;
    private List<Recommendation> recommendations;
    private LocalDateTime timestamp;

    @Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
    public static class Recommendation {
        private String songId;               // ★ Long → String
        private String title;
        private String artist;
        private String albumCover;
        private String previewUrl;
        private String spotify_embed_url;    // ★ 추가
    }
}
