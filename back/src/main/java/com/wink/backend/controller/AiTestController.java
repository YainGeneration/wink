package com.wink.backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class AiTestController {

    private final RestTemplate restTemplate;

    @Value("${ai.server.url}")
    private String aiServerUrl;

    public AiTestController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Operation(
        summary = "Flask AI 서버 연결 테스트",
        description = "Flask AI 추천 서버(`/api/recommend`)와 연결 상태를 확인합니다. "
                    + "AI 서버가 정상 응답하면 200 OK와 Flask 응답 본문을 반환합니다."
    )
    @GetMapping("/ai")
    public ResponseEntity<?> testAiConnection() {
        try {
            // ✅ 샘플 요청 본문
            Map<String, Object> payload = new HashMap<>();
            payload.put("sessionId", 999L);
            payload.put("topic", "테스트 주제");
            payload.put("inputText", "오늘은 평화로운 저녁이야");
            payload.put("imageUrls", new String[] {
                    "https://wink-bucket.s3.amazonaws.com/test_image.jpg"
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            // ✅ Flask 서버 호출
            ResponseEntity<String> response = restTemplate.exchange(
                    aiServerUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );

            return ResponseEntity.ok(Map.of(
                    "message", "✅ Flask AI 서버 연결 성공!",
                    "aiServerUrl", aiServerUrl,
                    "responseCode", response.getStatusCodeValue(),
                    "responseBody", response.getBody()
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                    "message", "❌ Flask AI 서버 연결 실패",
                    "aiServerUrl", aiServerUrl,
                    "error", e.getMessage()
            ));
        }
    }
}
