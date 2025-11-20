package com.wink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter @Setter
public class PlaylistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "playlist_id")
    private Playlist playlist;

    private String songId;
    private String title;
    private String artist;
    private String albumCover;
    private String previewUrl;
    private Long durationMs;
    private String durationFormatted;
    private String trackUrl;
    private String spotifyEmbedUrl;
}
