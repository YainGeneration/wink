package com.wink.backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ChatMessageRequest {
    private Long sessionId;           // 현재 대화 세션 ID
    private String sender;
    private String text;              // 사용자가 보낸 텍스트
    private List<String> imageUrls;   // 첨부 이미지 (있다면)

    @Override
    public String toString() {
        return "ChatMessageRequest{" +
                "sessionId=" + sessionId +
                ", sender='" + sender +
                ", text='" + text + '\'' +
                ", imageUrls=" + imageUrls +
                '}';
    }
}
