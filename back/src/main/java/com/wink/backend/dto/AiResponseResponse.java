package com.wink.backend.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class AiResponseResponse {

    // 세션 식별자
    private Long sessionId;

    // 세션 주제 (예: “Rainy mood jazz”)
    private String topic;

    // Flask에서 생성된 감성 키워드
    private List<String> keywords;

    // Flask에서 생성된 AI 메시지 (예: “비 오는 날 감성에 어울리는 음악 추천입니다.”)
    private String aiMessage;

    // Flask Agent3의 merge 결과 문장 (영문 문장)
    private String mergedSentence;

    // 추천된 곡 목록
    private List<Recommendation> recommendations;

    // 응답 생성 시각
    private LocalDateTime timestamp;

    // === 내부 클래스: 추천곡 객체 ===
    @Getter
    @Setter
    @Builder
    public static class Recommendation {
        private String title;       // 곡 제목
        private String artist;      // 아티스트
        private String albumCover;  // 앨범 커버 URL (옵션)
        private String previewUrl;  // 미리듣기 URL (옵션)
    }
}
