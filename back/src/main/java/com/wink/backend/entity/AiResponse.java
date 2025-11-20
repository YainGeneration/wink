package com.wink.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class AiResponse {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어떤 세션의 AI 응답인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private ChatSession session;

    private String topic;
    private String aiMessage;
    private String mergedSentence;
    private String interpretedSentence;

    private String englishText;
    private String englishCaption;
    private String imageDescriptionKo;

    // 추천 결과(JSON or TEXT)
    @Column(columnDefinition = "TEXT")
    private String recommendationsJson;

    private LocalDateTime timestamp = LocalDateTime.now();
}
