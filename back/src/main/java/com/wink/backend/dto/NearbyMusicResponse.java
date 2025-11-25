package com.wink.backend.dto;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class NearbyMusicResponse {
    private Long userId;
    private String nickname;
    private String songTitle;
    private String artist;
    // private String albumCover;
    private double lat;
    private double lng;
}