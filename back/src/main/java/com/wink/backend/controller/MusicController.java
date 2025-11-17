package com.wink.backend.controller;

import com.wink.backend.dto.MusicControlButton;
import com.wink.backend.dto.MusicDetailResponse;
import com.wink.backend.service.MusicService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/music")
public class MusicController {

    private final MusicService musicService;

    @Autowired
    public MusicController(MusicService musicService) {
        this.musicService = musicService;
    }
    @Operation(summary = "음악 상세 조회", description = "곡 ID로 가사, 앨범, 미리듣기, 가수 등 세부정보 반환")
    @GetMapping("/{songId}")
    public MusicDetailResponse getMusicDetail(@PathVariable Long songId) {
        return musicService.getMusicDetail(songId);
    }

    @Operation(summary = "음악 재생")
    @PostMapping("/play")
    public MusicControlButton playMusic(@RequestParam Long songId, @RequestParam Long sessionId) {
        return musicService.playMusic(songId, sessionId);
    }

    @Operation(summary = "일시정지")
    @PostMapping("/pause")
    public MusicControlButton pauseMusic(@RequestParam Long songId, @RequestParam Long sessionId) {
        return musicService.pauseMusic(songId, sessionId);
    }

    @Operation(summary = "다음 곡 재생")
    @PostMapping("/next")
    public MusicControlButton nextMusic(@RequestParam Long songId, @RequestParam Long sessionId) {
        return musicService.nextMusic(songId, sessionId);
    }
    
    @Operation(summary = "이전 곡 재생")
    @PostMapping("/prev")
    public MusicControlButton prevMusic(@RequestParam Long songId, @RequestParam Long sessionId) {
        return musicService.prevMusic(songId, sessionId);
    }

    @Operation(summary = "반복 재생 토글", description = "반복 재생을 켜거나 끕니다.")
    @PostMapping("/repeat")
    public String toggleRepeat() {
        return musicService.toggleRepeatMode();
    }

    @Operation(summary = "좋아요 추가")
    @PostMapping("/like")
    public MusicDetailResponse likeSong(@RequestParam Long songId) {
        return musicService.likeSong(songId);
    }
}
