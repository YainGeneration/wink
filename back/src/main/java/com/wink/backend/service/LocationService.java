package com.wink.backend.service;

import com.wink.backend.dto.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service

public class LocationService {


    public LocationResponse searchLocation(String query) {
        // TODO: 카카오맵 API 연동 가능, 지금은 mock 데이터
        return new LocationResponse(query, 37.5665, 126.9780, "서울특별시 중구 세종대로");
    }

    public List<NearbyMusicResponse> getNearbyMusic(double lat, double lng) {
        return List.of(
                new NearbyMusicResponse(1L, "사용자1", "Love Dive", "IVE", "cover1.jpg", lat + 0.001, lng + 0.001),
                new NearbyMusicResponse(2L, "사용자2", "ETA", "NewJeans", "cover2.jpg", lat - 0.001, lng - 0.001)
        );
    }

    public MusicDetailResponse getNearbyUserMusic(Long userId) {
        return new MusicDetailResponse(1001L, "Love Dive", "IVE", "After Like", "가사 예시...", "preview.mp3", 120000, 54000);
    }
}
