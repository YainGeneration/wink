package com.wink.backend.service;

import com.wink.backend.dto.*;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PlayerService {

    public PlaylistResponse getPlaylist(Long sessionId) {
        List<MusicDetailResponse> songs = List.of(
                new MusicDetailResponse(101L, "Love Dive", "IVE", "After Like", "", "preview1.mp3", 180000, 123000),
                new MusicDetailResponse(102L, "Blue Valentine", "NMIXX", "Blue Valentine", "", "preview2.mp3", 200000, 110000)
        );
        return new PlaylistResponse(sessionId, songs);
    }

    public ApiResponse savePlaylist(PlaylistSaveRequest req) {
        return new ApiResponse(true, "Playlist saved successfully for session " + req.getSessionId());
    }
}
