package com.wink.backend.dto;

import java.time.LocalDateTime;
import lombok.*;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor @Builder
public class ChatSearchResponse {

    private Long sessionId;
    private String topic;
    private String type;
    private String snippet;       // matchedMessage
    private LocalDateTime timestamp;     // session start time
    private String matchedMessage;
}
