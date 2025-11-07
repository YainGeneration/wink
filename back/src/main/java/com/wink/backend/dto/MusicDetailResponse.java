package com.wink.backend.dto;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class MusicDetailResponse {
    private Long songId;
    private String title;
    private String artist;
    private String album;
    private String lyrics;
    private String previewUrl;
    private int durationMs;
    private int likeCount;
}