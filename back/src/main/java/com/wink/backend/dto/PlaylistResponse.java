package com.wink.backend.dto;
import lombok.*;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PlaylistResponse {
    private Long sessionId;
    private List<MusicDetailResponse> songs;
}