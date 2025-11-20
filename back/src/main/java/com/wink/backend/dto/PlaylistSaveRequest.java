package com.wink.backend.dto;

import lombok.*;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaylistSaveRequest {

    private Long sessionId;
    private String playlistName;
    private List<TrackDto> tracks;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrackDto {
        private String songId;
        private String title;
        private String artist;
        private String albumCover;
        private String previewUrl;
        private Long durationMs;
        private String durationFormatted;
        private String trackUrl;
        private String spotifyEmbedUrl;
    }
}
