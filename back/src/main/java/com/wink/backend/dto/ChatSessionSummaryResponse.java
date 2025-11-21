package com.wink.backend.dto;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatSessionSummaryResponse {

    private Long sessionId;
    private String type;
    private String topic;
    private String latestMessage;

    private LocalDateTime timestamp;   // ← ★★ 이게 없어서 에러난 것
    private boolean isEnded;
    private LocalDateTime endTime;
}
