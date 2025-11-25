package com.wink.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AiResponseResponse {

    private Long sessionId;
    private String topic;

    private String inputText;
    private String imageBase64;

    private LocationResponse location;
    private List<ChatStartSpaceRequest.NearbyMusic> nearbyMusic;


    private List<String> keywords;
    private String aiMessage;

    private String mergedSentence;
    private String interpretedSentence;

    private String englishText;
    private String englishCaption;
    private String imageDescriptionKo;

    private List<Recommendation> recommendations;

    private LocalDateTime timestamp;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Recommendation {
        private String songId;
        private String title;
        private String artist;
        private String albumCover;
        private String previewUrl;

        private Long durationMs;
        private String durationFormatted;   // ⭐ 이 위치가 정답
        private String spotifyEmbedUrl;
        private String trackUrl;
        private String audioUrl;
    }
}
