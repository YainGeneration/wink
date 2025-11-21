package com.wink.backend.controller;
// import com.wink.backend.entity.ChatSession; // ChatController에서는 사용하지 않으므로 제거

import com.wink.backend.dto.*;
import com.wink.backend.service.ChatService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    // -----------------------------------
    // ① 새 채팅 시작
    // -----------------------------------
    @Operation(summary = "새 채팅 시작 (나의 순간) → 바로 AI 응답 반환")
    @PostMapping("/start/my")
    public ChatHistoryResponse startMy(@RequestBody ChatStartMyRequest req) {
        return chatService.startMy(req);
    }

    @Operation(summary = "새 채팅 시작 (공간의 순간) → 바로 AI 응답 반환")
    @PostMapping("/start/space")
    public ChatHistoryResponse startSpace(@RequestBody ChatStartSpaceRequest req) {
        return chatService.startSpace(req);
    }
    // -----------------------------------
    // ② AI 호출
    // -----------------------------------
    @Operation(summary = "AI 추천 응답 생성")
    @PostMapping("/ai-response")
    public AiResponseResponse generateAiResponse(@RequestBody AiResponseRequest req) {
        return chatService.generateAiResponse(req);
    }

    // -----------------------------------
    // ③ 후속 메시지 보내기 (최신 세션만 가능)
    // -----------------------------------
    @Operation(summary = "메시지 전송 (최신 세션만)")
    @PostMapping("/message")
    public ChatHistoryResponse sendUserMessage(@RequestBody ChatMessageRequest req) {
        return chatService.sendUserMessage(req);
    }

    // -----------------------------------
    // ④ 세션 전체 기록 조회 (모든 세션 가능)
    // -----------------------------------
    @Operation(summary = "채팅 전체 기록 조회 (모든 세션)")
    @GetMapping("/{sessionId}/full")
    public ChatHistoryResponse getFullChat(@PathVariable Long sessionId) {
        return chatService.getChatFullHistory(sessionId);
    }

    // -----------------------------------
    // ⑤ 최신이 아닌 세션 요약 조회
    // -----------------------------------
    @Operation(summary = "세션 요약 조회 (비활성화된 세션만)")
    @GetMapping("/{sessionId}/summary")
    public ChatSummaryResponse getSummary(@PathVariable Long sessionId) {
        // ChatService에서 활성화된 세션에 대한 요청을 차단함
        return chatService.getChatSummary(sessionId);
    }

    // -----------------------------------
    // ⑥ 세션 목록 조회
    // -----------------------------------
    @Operation(summary = "나의 순간 세션 목록 조회")
    @GetMapping("/sessions/my")
    public List<ChatSessionSummaryResponse> getMySessions() {
        return chatService.getMySessionList();
    }

    @Operation(summary = "공간의 순간 세션 목록 조회")
    @GetMapping("/sessions/space")
    public List<ChatSessionSummaryResponse> getSpaceSessions() {
        return chatService.getSpaceSessionList();
    }

    // -----------------------------------
    // ⑦ (프론트 편의를 위해) history/my / history/space는 full로 통일
    // -----------------------------------
    @Operation(summary = "나의 순간 전체 기록 조회")
    @GetMapping("/history/my/{sessionId}")
    public ChatHistoryResponse getMyHistory(@PathVariable Long sessionId) {
        return chatService.getChatFullHistory(sessionId);
    }

    @Operation(summary = "공간의 순간 전체 기록 조회")
    @GetMapping("/history/space/{sessionId}")
    public ChatHistoryResponse getSpaceHistory(@PathVariable Long sessionId) {
        return chatService.getChatFullHistory(sessionId);
    }

    @Operation(summary = "채팅 내 검색")
    @GetMapping("/search")
    public List<ChatSearchResponse> search(@RequestParam String q) {
        return chatService.search(q);
    }
    
}