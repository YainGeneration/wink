package com.wink.backend.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class MusicControlButton {
    private boolean success;
    private Long sessionId;
    private MusicDetailResponse currentSong;
    private String status;
}
