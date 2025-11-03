package com.wink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "session_id")
    private ChatSession session;

    private String sender; // "user" or "ai"
    @Column(columnDefinition = "TEXT")
    private String text;
    private String imageUrl;

    // ✅ AI 키워드와 추천곡을 JSON으로 저장
    @Column(columnDefinition = "TEXT")
    private String keywordsJson;

    @Column(columnDefinition = "TEXT")
    private String recommendationsJson;

    private LocalDateTime createdAt = LocalDateTime.now();
}
