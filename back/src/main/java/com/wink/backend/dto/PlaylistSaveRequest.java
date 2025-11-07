package com.wink.backend.dto;
import lombok.*;
import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PlaylistSaveRequest {
    private Long sessionId;
    private List<Long> songIds;
}