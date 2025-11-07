package com.wink.backend.controller;

import com.wink.backend.dto.*;
import com.wink.backend.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/player")
@CrossOrigin(origins = "*")
public class PlayerController {

    private final PlayerService playerService;
    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @Operation(summary = "AI 추천 재생목록 전체 보기", description = "세션 ID 기반으로 AI 추천곡 전체 반환")
    @GetMapping("/playlist")
    public PlaylistResponse getPlaylist(@RequestParam Long sessionId) {
        return playerService.getPlaylist(sessionId);
    }

    @Operation(summary = "AI 추천 재생목록 저장", description = "추천받은 곡들을 세션별로 저장")
    @PostMapping("/playlist/save")
    public ApiResponse savePlaylist(@RequestBody PlaylistSaveRequest req) {
        return playerService.savePlaylist(req);
    }
}
