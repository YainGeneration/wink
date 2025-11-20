package com.wink.backend.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private Long sessionId; 
    private String sender;
    private String text;
    private List<String> imageBase64; // ⭐ 이 필드를 추가해야 오류가 해결됩니다.
    private List<String> keywords;
    private List<AiResponseResponse.Recommendation> recommendations;
    private LocalDateTime timestamp;
}