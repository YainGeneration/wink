package com.wink.backend.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wink.backend.config.SingleBase64Deserializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private Long sessionId;           // 현재 대화 세션 ID
    private String sender;
    private String text;     
    @JsonDeserialize(using = SingleBase64Deserializer.class)
    private String imageBase64;

    @Override
    public String toString() {
        return "ChatMessageRequest{" +
                "sessionId=" + sessionId +
                ", sender='" + sender +
                ", text='" + text + '\'' +
                ", imageUrls=" + imageBase64 +
                '}';
    }
}
