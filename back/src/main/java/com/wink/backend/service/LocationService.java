package com.wink.backend.service;

import java.util.Collections;

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
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Set;
import java.util.HashSet;


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

    // ✅ 2. 주변 음악 조회 — 항상 동일한 5개의 Jamendo 곡을 반환
    public List<ChatStartSpaceRequest.NearbyMusic> getFixedNearbyMusic() {
        List<ChatStartSpaceRequest.NearbyMusic> list = new ArrayList<>();

        list.add(new ChatStartSpaceRequest.NearbyMusic(
                "track_1342724",
                "Yoga",
                "Spa Background Music",
                "../../public/img/near1.jpeg"
        ));

        list.add(new ChatStartSpaceRequest.NearbyMusic(
                "track_0376419",
                "To you my Love",
                "DANIEL H",
                "../../public/img/near2.jpeg"
        ));

        list.add(new ChatStartSpaceRequest.NearbyMusic(
                "track_1155551",
                "Jocelyn - Cyril Ury - MdC",
                "Cyril Ury",
                "../../public/img/near3.jpeg"
        ));

        list.add(new ChatStartSpaceRequest.NearbyMusic(
                "track_1244733",
                "The Last Stand",
                "Grégoire Lourme",
                "../../public/img/near4.jpeg"
        ));

        list.add(new ChatStartSpaceRequest.NearbyMusic(
                "track_1333716",
                "Movement IX - Fibonacci Theorem (Orchestral Version)",
                "JCRZ",
                "../../public/img/near5.jpeg"
        ));

        return list;
    }
    public List<ChatStartSpaceRequest.NearbyMusic> getNearbyMusic(double lat, double lng) {
        return getFixedNearbyMusic();
    }
}
