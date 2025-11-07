package com.wink.backend.dto;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class ChatSearchResultResponse {
    private Long sessionId;
    private String topic;
    private String snippet;
}