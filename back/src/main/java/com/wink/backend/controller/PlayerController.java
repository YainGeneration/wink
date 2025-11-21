package com.wink.backend.controller;

import com.wink.backend.dto.*;
import com.wink.backend.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;


@RestController
@RequestMapping("/api/player")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;

    @PostMapping("/playlist/save")
    public ApiResponse savePlaylist(@RequestBody PlaylistSaveRequest req) {
        return playerService.savePlaylist(req);
    }

    @GetMapping("/playlist/{id}")
    public PlaylistResponse getPlaylist(@PathVariable Long id) {
        return playerService.getPlaylist(id);
    }
}

