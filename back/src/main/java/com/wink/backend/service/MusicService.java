package com.wink.backend.service;

import com.wink.backend.dto.MusicControlButton;
import com.wink.backend.dto.MusicDetailResponse;
import com.wink.backend.entity.MusicEntity;
import com.wink.backend.repository.MusicRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MusicService {

    private final MusicRepository musicRepository;

    @Autowired
    public MusicService(MusicRepository musicRepository) {
        this.musicRepository = musicRepository;
    }

    /**
     * DB에서 songId로 음악 상세정보 조회
     */
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

    /**
     * 음악 재생
     */
    public MusicControlButton playMusic(Long songId, Long sessionId) {
        MusicDetailResponse currentSong = getMusicDetail(songId);

        return new MusicControlButton(
                true,
                sessionId,
                currentSong,
                "playing"
        );
    }

    /**
     * 음악 일시정지
     */
    public MusicControlButton pauseMusic(Long songId, Long sessionId) {
        MusicDetailResponse currentSong = getMusicDetail(songId);

        return new MusicControlButton(
                true,
                sessionId,
                currentSong,
                "paused"
        );
    }

    /**
     * 다음 곡 재생
     */
    public MusicControlButton nextMusic(Long currentSongId, Long sessionId) {
        List<MusicEntity> allSongs = musicRepository.findAll();

        if (allSongs.isEmpty()) {
            throw new RuntimeException("재생 가능한 곡이 없습니다.");
        }

        // 현재 곡 인덱스 찾기
        int currentIndex = -1;
        for (int i = 0; i < allSongs.size(); i++) {
            if (allSongs.get(i).getSongId().equals(currentSongId)) {
                currentIndex = i;
                break;
            }
        }

        // 다음 곡 인덱스 (마지막이면 첫 곡으로)
        int nextIndex = (currentIndex + 1) % allSongs.size();
        MusicEntity nextSong = allSongs.get(nextIndex);

        MusicDetailResponse nextSongDetail = new MusicDetailResponse(
                nextSong.getSongId(),
                nextSong.getTitle(),
                nextSong.getArtist(),
                nextSong.getAlbum(),
                nextSong.getLyrics(),
                nextSong.getPreviewUrl(),
                nextSong.getDurationMs(),
                nextSong.getLikeCount()
        );

        return new MusicControlButton(
                true,
                sessionId,
                nextSongDetail,
                "playing"
        );
    }

    /**
     * 좋아요 추가 (likeCount 1 증가)
     */
    public MusicDetailResponse likeSong(Long songId) {
        MusicEntity entity = musicRepository.findById(songId)
                .orElseThrow(() -> new RuntimeException("노래를 찾을 수 없습니다. songId=" + songId));

        entity.setLikeCount(entity.getLikeCount() + 1);
        musicRepository.save(entity); // 변경사항 DB 반영

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
}
