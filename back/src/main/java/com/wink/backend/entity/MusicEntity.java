package com.wink.backend.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "music")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MusicEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long songId;

    private String title;
    private String artist;
    private String album;
    private String lyrics;
    private String previewUrl;
    private int durationMs;
    private int likeCount;
}
