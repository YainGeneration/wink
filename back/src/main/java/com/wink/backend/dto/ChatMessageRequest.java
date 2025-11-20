package com.wink.backend.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wink.backend.config.SingleBase64Deserializer;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {

    private Long sessionId;     // 세션 ID
    private String text;        // 사용자 메시지 텍스트

    @JsonDeserialize(using = SingleBase64Deserializer.class)
    private String imageBase64; // Base64 문자열

    @Override
    public String toString() {
        return "ChatMessageRequest{" +
                "sessionId=" + sessionId +
                ", text='" + text + '\'' +
                ", imageBase64='" + 
                     (imageBase64 != null ? "length=" + imageBase64.length() : null)
                     + '\'' +
                '}';
    }
}
