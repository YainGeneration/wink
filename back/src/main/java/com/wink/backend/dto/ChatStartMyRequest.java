package com.wink.backend.dto;

import java.util.List;

import lombok.Data;

@Data
public class ChatStartMyRequest {
    private String inputText;
    private List<String> imageBase64;  
}
