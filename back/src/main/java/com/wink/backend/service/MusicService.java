package com.wink.backend.service;

import com.wink.backend.dto.MusicControlButton;
import com.wink.backend.dto.MusicDetailResponse;
import com.wink.backend.entity.MusicEntity;
import com.wink.backend.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class MusicService {

    private final MusicRepository musicRepository;

    // ✅ 반복 재생 상태 (전역 toggle)
    private final AtomicBoolean repeatMode = new AtomicBoolean(false);

    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    public MusicDetailResponse getMusicDetail(Long songId) {
        MusicEntity entity = musicRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("노래를 찾을 수 없습니다. songId=" + songId));

        return new MusicDetailResponse(
                entity.getSongId(),
                entity.getTitle(),
                entity.getArtist(),
                entity.getAlbum(),
                entity.getLyrics(),
                entity.getPreviewUrl(),
                entity.getDurationMs(),
                entity.getLikeCount()
        );
    }

    public MusicControlButton playMusic(Long songId, Long sessionId) {
        MusicDetailResponse currentSong = getMusicDetail(songId);
        return new MusicControlButton(true, sessionId, currentSong, "playing");
    }

    public MusicControlButton pauseMusic(Long songId, Long sessionId) {
        MusicDetailResponse currentSong = getMusicDetail(songId);
        return new MusicControlButton(true, sessionId, currentSong, "paused");
    }

    public MusicControlButton nextMusic(Long currentSongId, Long sessionId) {
        List<MusicEntity> allSongs = musicRepository.findAll();
        if (allSongs.isEmpty()) throw new RuntimeException("재생 가능한 곡이 없습니다.");

        int currentIndex = findIndex(allSongs, currentSongId);
        int nextIndex = (currentIndex + 1) % allSongs.size();
        return makeButton(allSongs.get(nextIndex), sessionId, "playing");
    }

    // ✅ 이전 곡 재생
    public MusicControlButton prevMusic(Long currentSongId, Long sessionId) {
        List<MusicEntity> allSongs = musicRepository.findAll();
        if (allSongs.isEmpty()) throw new RuntimeException("재생 가능한 곡이 없습니다.");

        int currentIndex = findIndex(allSongs, currentSongId);
        int prevIndex = (currentIndex - 1 + allSongs.size()) % allSongs.size();
        return makeButton(allSongs.get(prevIndex), sessionId, "playing");
    }

    // ✅ 반복 재생 토글
    public String toggleRepeatMode() {
        boolean newValue = !repeatMode.get();
        repeatMode.set(newValue);
        return newValue ? "repeat_on" : "repeat_off";
    }

    public MusicDetailResponse likeSong(Long songId) {
        MusicEntity entity = musicRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("노래를 찾을 수 없습니다. songId=" + songId));

        entity.setLikeCount(entity.getLikeCount() + 1);
        musicRepository.save(entity);

        return new MusicDetailResponse(
                entity.getSongId(),
                entity.getTitle(),
                entity.getArtist(),
                entity.getAlbum(),
                entity.getLyrics(),
                entity.getPreviewUrl(),
                entity.getDurationMs(),
                entity.getLikeCount()
        );
    }

    // ======= Helper Methods =======
    private int findIndex(List<MusicEntity> songs, Long songId) {
        for (int i = 0; i < songs.size(); i++) {
            if (songs.get(i).getSongId().equals(songId)) return i;
        }
        return 0;
    }

    private MusicControlButton makeButton(MusicEntity song, Long sessionId, String status) {
        MusicDetailResponse detail = new MusicDetailResponse(
                song.getSongId(),
                song.getTitle(),
                song.getArtist(),
                song.getAlbum(),
                song.getLyrics(),
                song.getPreviewUrl(),
                song.getDurationMs(),
                song.getLikeCount()
        );
        return new MusicControlButton(true, sessionId, detail, status);
    }
}
