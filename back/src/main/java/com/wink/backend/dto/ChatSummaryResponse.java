package com.wink.backend.dto;
import lombok.*;
import java.util.List;

@Getter @Setter @Builder @AllArgsConstructor @NoArgsConstructor
public class ChatSummaryResponse {
    private Long sessionId;
    private String topic;
    private String summaryText;
    private List<String> keywords;
    private List<AiResponseResponse.Recommendation> recommendations;
}