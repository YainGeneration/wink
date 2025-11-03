package com.wink.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiResponseResponse {

    private Long sessionId;
    private String topic;
    private List<String> keywords;
    private String aiMessage;
    private List<Recommendation> recommendations;
    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private Long songId;         // ✅ songId가 없어도 null 허용
        private String title;
        private String artist;
        private String albumCover;
        private String previewUrl;
    }
}
