package com.wink.backend.controller;

import com.wink.backend.dto.*;
import com.wink.backend.service.ChatExtraService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatExtraController {

    private final ChatExtraService chatExtraService;

    public ChatExtraController(ChatExtraService chatExtraService) {
        this.chatExtraService = chatExtraService;
    }

    @Operation(summary = "세션 내 대화 요약 조회", description = "Gemini 기반으로 세션 전체를 요약하여 주요 내용/키워드/추천 결과를 반환")
    @GetMapping("/summary/{sessionId}")
    public ChatSummaryResponse getChatSummary(@PathVariable Long sessionId) {
        return chatExtraService.getChatSummary(sessionId);
    }

    @Operation(summary = "채팅 내 검색", description = "모든 세션의 주제/입력 텍스트/AI 응답 중 키워드가 포함된 항목 반환")
    @GetMapping("/search")
    public List<ChatSearchResultResponse> searchChat(@RequestParam String keyword) {
        return chatExtraService.searchChat(keyword);
    }
}
