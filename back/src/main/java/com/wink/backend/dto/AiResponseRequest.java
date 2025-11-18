package com.wink.backend.dto;

import lombok.Data;
import java.util.List;

@Data
public class AiResponseRequest {

    private Long sessionId;          // 세션 ID
    private String inputText;        // 사용자 텍스트
    private List<String> imageBase64; // Base64 이미지 (변경됨!)
}
