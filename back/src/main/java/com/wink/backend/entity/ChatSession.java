package com.wink.backend.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "MY" 또는 "SPACE"
    private String type;

    // Gemini가 뽑은 주제
    private String topic;

    // 세션 시작/종료 시각
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    private Double startLat;
    private Double startLng;

    // 진행 중(false) / 종료(true) 표시
    @Column(nullable = false)
    private Boolean isEnded = false;

    @PrePersist
    public void onCreate() {
        if (this.startTime == null) {
            this.startTime = LocalDateTime.now();
        }
        if (this.isEnded == null) {
            this.isEnded = false;
        }
    }
}
