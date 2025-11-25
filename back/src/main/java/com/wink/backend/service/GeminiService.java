package com.wink.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import java.net.http.*;
import java.net.URI;
import java.time.Duration;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.*;

/**
 * GeminiService
 * - Google Gemini API를 이용한 주제 추출, 요약, 키워드 번역 기능 제공
 */
@Service
public class GeminiService {

private static final String GEMINI_MODEL = "gemini-2.0-flash-lite";

private static final String GEMINI_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/" 
    + GEMINI_MODEL + ":generateContent";

    @Value("${GEMINI_API_KEY:#{null}}")
    private String apiKey;

    private static final ObjectMapper mapper = new ObjectMapper();

    /** ✅ 연결 상태 점검용 */
    public void checkApiConnection() {
        System.out.println("--------------------------------------------------");
        System.out.println("🔍 Gemini API 연결 상태 확인");
        System.out.println("📡 엔드포인트 URL: " + GEMINI_URL);
        if (apiKey == null || apiKey.isBlank()) {
            System.out.println("❌ GEMINI_API_KEY 인식 안 됨 (환경변수 또는 properties 확인 필요)");
        } else {
            System.out.println("✅ GEMINI_API_KEY 인식됨 (길이: " + apiKey.length() + "자)");
        }
        System.out.println("--------------------------------------------------");
    }

    /** ✅ 입력 텍스트로부터 핵심 주제(topic) 도출 */
    public String extractTopic(String inputText) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                System.err.println("❌ GEMINI_API_KEY is not set. Using fallback.");
                return fallbackTopic(inputText);
            }

            String prompt = "입력된 문장애 있는 시간, 공간, 위치, 감정 상황 정보를 바탕으로 제목처럼 지어줘. " +
                    "입력받은 음악 제목과 가수 이름을 제목에 절대 포함하지마. 음악의 장르나 분위기만 반영해줘. " +
                    "꼭 필요한 문장 기호가 아닌 이상 넣지 마. " +
                    "일반적으로 요약하지 말고 input text의 특성을 살려서 제목 만들어줘 문장: \"" + inputText + "\"";

            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );
            String requestBody = mapper.writeValueAsString(jsonBody);


            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            System.out.println("📨 Gemini 요청: " + prompt);
            System.out.println("✅ Gemini 응답 코드: " + response.statusCode());
            System.out.println("✅ Gemini 응답 본문: " + response.body());

            if (response.statusCode() != 200) {
                System.err.println("⚠️ Gemini API 호출 실패 (" + response.statusCode() + ")");
                return fallbackTopic(inputText);
            }

            JsonNode root = mapper.readTree(response.body());
            JsonNode textNode = root.path("candidates").get(0)
                    .path("content").path("parts").get(0).path("text");

            if (textNode.isMissingNode() || textNode.asText().isBlank()) {
                System.err.println("⚠️ Gemini 응답에 주제 텍스트 없음");
                return fallbackTopic(inputText);
            }

            return textNode.asText().trim();

        } catch (Exception e) {
            e.printStackTrace();
            return fallbackTopic(inputText);
        }
    }

    /** ✅ 영어 키워드 리스트를 한국어 감성 단어로 번역 */
    public List<String> translateKeywords(List<String> englishKeywords) {
        try {
            if (apiKey == null || apiKey.isBlank() || englishKeywords == null || englishKeywords.isEmpty()) {
                System.out.println("⚠️ GEMINI_API_KEY 없음 또는 번역할 키워드 없음 → 원본 유지");
                return englishKeywords;
            }

            String joined = String.join(", ", englishKeywords);
            String prompt = "다음 영어 단어들을 감성적인 한국어 단어로 번역해줘. " + "단, 입력된 모든 단어를 반드시 번역해. 의미가 약하면 의미를 보정해도 괜찮아." +
                    "단, 개수와 순서는 반드시 유지하고, **다른 설명이나 문장 부호 없이 오직 쉼표(,)로만 구분해서** 출력해줘. 단어들: " + joined;

            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );
            String requestBody = mapper.writeValueAsString(jsonBody);


            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("⚠️ 키워드 번역 실패 (" + response.statusCode() + ")");
                return englishKeywords;
            }

            JsonNode root = mapper.readTree(response.body());
            String text = root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("⚠️ 키워드 번역 중 오류 발생");
            return englishKeywords;
        }
    }

    /** ✅ Gemini API 실패 시 간단한 규칙 기반 주제 추출 */
    private String fallbackTopic(String text) {
        text = text == null ? "" : text;
        if (text.contains("비")) return "비 오는 날 감성";
        if (text.contains("집중")) return "집중용 재즈";
        if (text.contains("산책")) return "산책할 때 듣는 음악";
        if (text.contains("퇴근")) return "퇴근길 플레이리스트";
        if (text.contains("밤")) return "밤 감성 음악";
        if (text.contains("사랑")) return "로맨틱한 분위기 음악";
        return "오늘의 감성 음악";
    }

    /** ✅ 대화 전체 요약 */
    public String summarizeConversation(String allText) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "Gemini API Key가 설정되지 않았습니다.";
            }

            String prompt = "다음은 사용자의 대화 기록입니다. 핵심 내용을 3문장 이내로 간략히 요약해줘:\n" + allText;

            // String requestBody = String.format("""
            //     {
            //       "contents": [ { "parts": [ { "text": "%s" } ] } ]
            //     }
            // """, prompt.replace("\"", "'"));
            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            String requestBody = mapper.writeValueAsString(jsonBody);


            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("⚠️ 대화 요약 실패 (" + response.statusCode() + ")");
                return "대화 요약 실패: " + response.statusCode();
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText("요약 결과 없음");

        } catch (Exception e) {
            e.printStackTrace();
            return "요약 중 오류 발생";
        }
    }

    /** ✅ 요약문 기반 키워드 추출 */
    public List<String> extractKeywords(String summary) {
        try {
            if (apiKey == null || apiKey.isBlank()) {
                return List.of("요약", "대화", "결과");
            }

            String prompt = "다음 요약문에서 주요 키워드 3~5개를 추출해줘. 쉼표로만 구분해서 출력해줘:\n" + summary;

            // String requestBody = String.format("""
            //     {
            //       "contents": [ { "parts": [ { "text": "%s" } ] } ]
            //     }
            // """, prompt.replace("\"", "'"));
            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );
            String requestBody = mapper.writeValueAsString(jsonBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                System.err.println("⚠️ 키워드 추출 실패 (" + response.statusCode() + ")");
                return List.of("요약", "실패");
            }

            JsonNode root = mapper.readTree(response.body());
            String text = root.path("candidates").get(0)
                              .path("content").path("parts").get(0)
                              .path("text").asText();

            return Arrays.stream(text.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .toList();

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("오류", "발생");
        }
    }
    /** 
     * 🎵 mergedSentence(영문) → 한국어 감성 해석문 변환
     */
    public String interpretMergedSentence(String mergedSentence) {

        if (mergedSentence == null || mergedSentence.isBlank()) {
            return "감성 해석문을 생성할 문장이 없습니다.";
        }

        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "Gemini API Key가 설정되지 않았습니다.";
            }

            String prompt =
                    "다음 문장을 자연스러운 한국어 감성 문장으로 해석해줘. " + "문장에서 suffraget university 이런식의 한국에 없는 것들은 빼줘" +
                    "직역하지 말고 문맥의 분위기, 감정, 정서를 담아 한 문장으로 표현하되, '~해서 추천합니다.' 형식으로 출력해줘':\n" 
                    + mergedSentence;

            // ... (HTTP 요청 본문 구성 및 HttpClient 설정 코드 생략)

            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );
            String requestBody = mapper.writeValueAsString(jsonBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(12))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // [수정]: 오류 발생 시 상세 상태 코드 출력
            if (response.statusCode() != 200) {
                System.err.println("⚠️ mergedSentence 해석 실패: HTTP Status Code " + response.statusCode());
                // 필요하다면 응답 본문까지 출력하여 Gemini의 에러 메시지 확인
                // System.err.println("Gemini Error Body: " + response.body()); 
                
                // HTTP 실패와 일반 오류 메시지를 분리하여 반환
                return "감성 해석 생성 실패 (HTTP:" + response.statusCode() + ")";
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText("해석 결과 없음");

        } catch (Exception e) {
            // [수정]: 일반 오류 발생 시, 오류 로그와 메시지 분리
            e.printStackTrace();
            System.err.println("❌ 감성 해석 중 일반 오류 발생: " + e.getMessage());
            return "감성 해석 중 오류 발생 (Exception)";
        }
    }
    /**
     * 🎨 이미지 캡션(english_caption)을 자연스러운 한국어 문장으로 번역
     */
    public String translateToKorean(String englishText) {

        if (englishText == null || englishText.isBlank()) {
            return null;
        }

        try {
            if (apiKey == null || apiKey.isBlank()) {
                return englishText; // fallback: 영어 그대로 반환
            }

            String prompt = "다음 영어 문장을 자연스러운 한국어 문장으로 번역해줘. " +
                    "직역 말고 분위기와 감정을 살려서 부드럽게 표현해되, 존댓말로 '~합니다.'로 답해:\n" + englishText;

            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );

            String requestBody = mapper.writeValueAsString(jsonBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("⚠️ translateToKorean 실패 (" + response.statusCode() + ")");
                return englishText; // fallback
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText(englishText);

        } catch (Exception e) {
            e.printStackTrace();
            return englishText; // fallback
        }
    }

    /**
     * 🎯 추가된 메서드: 단일 문장(최신 사용자 메시지) 요약
     * @param inputText 요약할 단일 문장
     * @return 요약된 결과
     */
    public String summarizeSentence(String inputText) {
        if (inputText == null || inputText.isBlank()) {
            return "메시지 내용이 없습니다.";
        }

        try {
            if (apiKey == null || apiKey.isBlank()) {
                return "Gemini API Key가 설정되지 않았습니다.";
            }

            String prompt = "다음 문장을 음악 감성과 관련된 핵심 키워드를 중심으로 5단어 이내로 요약해줘:\n" + inputText;

            // String requestBody = String.format("""
            //     {
            //       "contents": [ { "parts": [ { "text": "%s" } ] } ]
            //     }
            // """, prompt.replace("\"", "'"));
            Map<String, Object> jsonBody = Map.of(
                    "contents", List.of(
                            Map.of(
                                    "parts", List.of(
                                            Map.of("text", prompt)
                                    )
                            )
                    )
            );
            String requestBody = mapper.writeValueAsString(jsonBody);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(GEMINI_URL + "?key=" + apiKey))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                System.err.println("⚠️ 단일 문장 요약 실패 (" + response.statusCode() + ")");
                return "단일 문장 요약 실패";
            }

            JsonNode root = mapper.readTree(response.body());
            return root.path("candidates").get(0)
                       .path("content").path("parts").get(0)
                       .path("text").asText("요약 결과 없음");

        } catch (Exception e) {
            e.printStackTrace();
            return "요약 중 오류 발생";
        }
    }
}