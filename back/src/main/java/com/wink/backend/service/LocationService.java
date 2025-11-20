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
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;


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

        int peopleCount = 5; // 다섯 명 고정


        // ----- 장르별 곡 풀 -----
        List<String[]> kpop = List.of(
            new String[]{"Love Dive", "IVE", "cover_ivedive.jpg"},
            new String[]{"ETA", "NewJeans", "cover_eta.jpg"},
            new String[]{"Super Shy", "NewJeans", "cover_supershy.jpg"},
            new String[]{"Ditto", "NewJeans", "cover_ditto.jpg"},
            new String[]{"Antifragile", "LE SSERAFIM", "cover_ls.jpg"},
            new String[]{"Spicy", "aespa", "cover_spicy.jpg"},
            new String[]{"Queencard", "(G)I-DLE", "cover_idle.jpg"},
            new String[]{"Cupid", "FIFTY FIFTY", "cover_cupid.jpg"},
            new String[]{"Fast Forward", "Jeon Somi", "cover_somi.jpg"},
            new String[]{"Hype Boy", "NewJeans", "cover_hypeboy.jpg"},
            new String[]{"Kitsch", "IVE", "cover_kitsch.jpg"},
            new String[]{"Baggy Jeans", "NCT U", "cover_nct.jpg"}
        );

        List<String[]> pop = List.of(
            new String[]{"As It Was", "Harry Styles", "cover_asitwas.jpg"},
            new String[]{"Anti-Hero", "Taylor Swift", "cover_taylor.jpg"},
            new String[]{"Blinding Lights", "The Weeknd", "cover_weeknd.jpg"},
            new String[]{"Good 4 U", "Olivia Rodrigo", "cover_good4u.jpg"},
            new String[]{"Peaches", "Justin Bieber", "cover_peaches.jpg"},
            new String[]{"Levitating", "Dua Lipa", "cover_dua.jpg"},
            new String[]{"Shivers", "Ed Sheeran", "cover_shivers.jpg"},
            new String[]{"Stay", "The Kid LAROI", "cover_stay.jpg"},
            new String[]{"Monaco", "Bad Bunny", "cover_monaco.jpg"}
        );

        List<String[]> jpop = Arrays.asList(
            new String[]{"Pretender", "Official Hige Dandism", "cover_hige.jpg"},
            new String[]{"Nandemonaiya", "RADWIMPS", "cover_rad.jpg"},
            new String[]{"Lemon", "Kenshi Yonezu", "cover_lemon.jpg"}
        );

        // ----- 장르 비율 -----
        double pKpop = 0.70;
        double pPop = 0.29;
        double pJpop = 0.01;

        List<NearbyMusicResponse> result = new ArrayList<>();

        for (int i = 1; i <= peopleCount; i++) {

            // 반경 100m = lat/lng ±0.0009
            double offsetLat = (rand.nextDouble() * 0.0018) - 0.0009;
            double offsetLng = (rand.nextDouble() * 0.0018) - 0.0009;

            double newLat = lat + offsetLat;
            double newLng = lng + offsetLng;

            // 장르 선택
            double r = rand.nextDouble();
            String[] song;
            if (r < pKpop) song = kpop.get(rand.nextInt(kpop.size()));
            else if (r < pKpop + pPop) song = pop.get(rand.nextInt(pop.size()));
            else song = jpop.get(rand.nextInt(jpop.size()));

            result.add(new NearbyMusicResponse(
                    (long) i,
                    "사용자" + i,
                    song[0],
                    song[1],
                    song[2],
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
