package com.wink.backend.dto;

import lombok.Data;

@Data
public class ChatStartMyRequest {
    private String inputText;
    private String imageBase64;  
}
