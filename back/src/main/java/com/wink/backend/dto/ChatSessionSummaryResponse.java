package com.wink.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ChatSessionSummaryResponse {

    private Long sessionId;
    private String type;
    private String topic;
    private String latestMessage;
    private LocalDateTime createdAt;

}
