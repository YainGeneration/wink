package com.wink.backend.controller;

import com.wink.backend.dto.*;
import com.wink.backend.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/location")
@CrossOrigin(origins = "*")
public class LocationController {

    private final LocationService locationService;
    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @Operation(summary = "위치 검색", description = "장소명으로 위도, 경도 검색")
    @GetMapping("/search")
    public LocationResponse searchLocation(@RequestParam String query) {
        return locationService.searchLocation(query);
    }

    @Operation(summary = "주변 음악 조회", description = "현재 위치 기준 주변 사용자의 재생 음악 목록 반환")
    @GetMapping("/nearby-music")
    public List<ChatStartSpaceRequest.NearbyMusic> getNearbyMusic(
            @RequestParam double lat,
            @RequestParam double lng
    ) {
        return locationService.getNearbyMusic(lat, lng);
    }

}
