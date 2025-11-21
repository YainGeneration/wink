package com.wink.backend.dto;

import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.wink.backend.config.SingleBase64Deserializer;

import lombok.Data;

@Data
public class AiResponseRequest {

    private Long sessionId;
    private String inputText;

    @JsonDeserialize(using = SingleBase64Deserializer.class)
    private String imageBase64;   // 단일 문자열!

    private ChatStartSpaceRequest.Location location;
    private List<ChatStartSpaceRequest.NearbyMusic> nearbyMusic;

}
