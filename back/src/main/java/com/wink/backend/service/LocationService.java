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


    // ✅ 2. 주변 음악 조회 (현재는 mock 유지)
    public List<NearbyMusicResponse> getNearbyMusic(double lat, double lng) {

        Random rand = new Random();
        int peopleCount = 5;

        // ----- POP 곡 풀 -----
        List<String[]> pop = List.of(
            new String[]{"As It Was", "Harry Styles"},
            new String[]{"Anti-Hero", "Taylor Swift"},
            new String[]{"Blinding Lights", "The Weeknd"},
            new String[]{"Good 4 U", "Olivia Rodrigo"},
            new String[]{"Peaches", "Justin Bieber"},
            new String[]{"Levitating", "Dua Lipa"},
            new String[]{"Shivers", "Ed Sheeran"},
            new String[]{"Stay", "The Kid LAROI"},
            new String[]{"Monaco", "Bad Bunny"}
        );

        // ----- JPOP 곡 풀 -----
        List<String[]> jpop = Arrays.asList(
            new String[]{"Pretender", "Official Hige Dandism"},
            new String[]{"Nandemonaiya", "RADWIMPS"},
            new String[]{"Lemon", "Kenshi Yonezu"}
        );

        // ----- POP + JPOP 전체 풀 -----
        List<String[]> songPool = new ArrayList<>();
        songPool.addAll(pop);
        songPool.addAll(jpop);

        // 곡이 5곡 이상인지 체크 (문제 없음)
        if (songPool.size() < peopleCount) {
            throw new RuntimeException("곡의 개수가 peopleCount보다 적습니다.");
        }

        // 곡 중복 방지 → 리스트 전체 shuffle
        Collections.shuffle(songPool);

        // 이제 songPool.get(i) 로 0~4까지 5곡이 모두 다르게 나옴

        // ----- 이미지 중복 방지 -----
        Set<Integer> usedImageNumbers = new HashSet<>();

        List<NearbyMusicResponse> result = new ArrayList<>();

        for (int i = 1; i <= peopleCount; i++) {

            // 위치 랜덤 offset
            double offsetLat = (rand.nextDouble() * 0.0018) - 0.0009;
            double offsetLng = (rand.nextDouble() * 0.0018) - 0.0009;

            double newLat = lat + offsetLat;
            double newLng = lng + offsetLng;

            // 중복 없는 곡 선택
            String[] song = songPool.get(i - 1); // shuffle된 순서대로 배정됨

            // 중복되지 않는 랜덤 이미지 번호 생성
            int randomImageNumber;
            do {
                randomImageNumber = rand.nextInt(10000) + 1;
            } while (usedImageNumbers.contains(randomImageNumber));

            usedImageNumbers.add(randomImageNumber);

            String profileImageUrl = "https://picsum.photos/200/200?random=" + randomImageNumber;

            result.add(new NearbyMusicResponse(
                    (long) i,
                    "사용자" + i,
                    song[0],           // title
                    song[1],           // artist
                    profileImageUrl,
                    newLat,
                    newLng
            ));
        }

        return result;
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
