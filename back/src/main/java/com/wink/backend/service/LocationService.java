package com.wink.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wink.backend.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class LocationService {

    @Value("${kakao.api.key}")
    private String kakaoApiKey; // ✅ application.properties에 추가해야 함

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    // ✅ 1. 주소 → 좌표 검색 (Kakao API 사용)
    public LocationResponse searchLocation(String query) {
        try {
            String encoded = URLEncoder.encode(query, StandardCharsets.UTF_8);
            String url = "https://dapi.kakao.com/v2/local/search/address.json?query=" + encoded;

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

            JsonNode root = mapper.readTree(response.getBody());
            JsonNode document = root.path("documents").get(0);

            double lat = document.path("y").asDouble();
            double lng = document.path("x").asDouble();
            String address = document.path("address_name").asText();

            return new LocationResponse(query, lat, lng, address);

        } catch (Exception e) {
            e.printStackTrace();
            // 실패 시 기본값 (서울시청 좌표)
            return new LocationResponse(query, 37.5665, 126.9780, "주소를 찾을 수 없습니다");
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
