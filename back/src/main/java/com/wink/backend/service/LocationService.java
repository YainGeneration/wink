package com.wink.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wink.backend.dto.*;

import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;


    // ✅ 1. 주소 → 좌표 검색 (Kakao API 사용)
@Service
public class LocationService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        restTemplate.getMessageConverters().stream()
                .filter(c -> c instanceof org.springframework.http.converter.StringHttpMessageConverter)
                .findFirst()
                .ifPresent(c -> ((org.springframework.http.converter.StringHttpMessageConverter) c)
                        .setDefaultCharset(StandardCharsets.UTF_8));
    }

    public LocationResponse searchLocation(String query) {
        try {
            String baseUrl = "https://dapi.kakao.com/v2/local/search";
            String keywordUrl = baseUrl + "/keyword.json?query=" + query;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            headers.set("Accept-Charset", "UTF-8");

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(keywordUrl, HttpMethod.GET, entity, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode docs = root.path("documents");
            System.out.println("📦 keyword.json 결과: " + docs);

            if (docs.isArray() && docs.size() > 0) {
                JsonNode doc = docs.get(0);
                return new LocationResponse(
                        doc.path("place_name").asText(),
                        doc.path("y").asDouble(),
                        doc.path("x").asDouble(),
                        doc.path("address_name").asText()
                );
            }

            System.out.println("⚠️ Kakao API 결과 없음: " + query);
            return new LocationResponse(query, 37.5665, 126.9780, "주소를 찾을 수 없습니다");

        } catch (Exception e) {
            e.printStackTrace();
            return new LocationResponse(query, 37.5665, 126.9780, "오류 발생");
        }
    }


    // ✅ 2. 주변 음악 조회 (현재는 mock 유지)
    public List<NearbyMusicResponse> getNearbyMusic(double lat, double lng) {
        return List.of(
                new NearbyMusicResponse(1L, "사용자1", "Love Dive", "IVE", "cover1.jpg", lat + 0.001, lng + 0.001),
                new NearbyMusicResponse(2L, "사용자2", "ETA", "NewJeans", "cover2.jpg", lat - 0.001, lng - 0.001)
        );
    }

    // ✅ 3. 특정 사용자 음악 상세 조회 (mock 유지)
    public MusicDetailResponse getNearbyUserMusic(Long userId) {
        return new MusicDetailResponse(
                1001L,
                "Love Dive",
                "IVE",
                "After Like",
                "가사 예시...",
                "preview.mp3",
                120000,
                54000
        );
    }
}
