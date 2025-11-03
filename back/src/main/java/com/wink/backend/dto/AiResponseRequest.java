package com.wink.backend.dto;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
public class AiResponseRequest {

    // 현재 대화 세션 ID
    private Long sessionId;

    // Flask 내부에서 참고하는 주제 (예: “Rainy mood jazz”)
    private String topic;

    // 사용자가 입력한 문장 (예: “비 오는 날 잔잔한 노래 추천해줘”)
    private String inputText;

    // 업로드되거나 선택된 이미지들의 URL (Flask Agent2에 전달)
    private List<String> imageUrls;
}
