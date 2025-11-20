package com.wink.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatHistoryResponse {
    private Long sessionId;
    private String type;
    private String topic;
    // 최신 세션인지 여부 (필요 없으면 null 로 둬도 됨)
    private Boolean isLatest;
    private List<ChatMessageResponse> messages;
}
