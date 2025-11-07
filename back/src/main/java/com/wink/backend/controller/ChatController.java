package com.wink.backend.controller;

import com.wink.backend.dto.AiResponseRequest;
import com.wink.backend.dto.AiResponseResponse;
import com.wink.backend.dto.ChatHistoryResponse;
import com.wink.backend.dto.ChatMessageRequest;
import com.wink.backend.dto.ChatMessageResponse;
import com.wink.backend.dto.ChatStartMyRequest;
import com.wink.backend.dto.ChatStartResponse;
import com.wink.backend.dto.ChatStartSpaceRequest;
import com.wink.backend.dto.ChatSummaryResponse;
import com.wink.backend.service.ChatService;
import com.wink.backend.dto.ChatSearchResultResponse;
import io.swagger.v3.oas.annotations.Operation;

import java.util.List;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Operation(summary = "새 채팅 시작 (나의 순간)", description = "입력된 문장에서 핵심 키워드로 topic 추출")
    @PostMapping("/start/my")
    public ChatStartResponse startMy(@RequestBody ChatStartMyRequest req) {
        return chatService.startMy(req);
    }

    @Operation(summary = "새 채팅 시작 (공간의 순간)", description = "장소/사진/주변 음악 정보를 기반으로 공간 분위기 topic 설정")
    @PostMapping("/start/space")
    public ChatStartResponse startSpace(@RequestBody ChatStartSpaceRequest req) {
        return chatService.startSpace(req);
    }

    @Operation(summary = "AI 추천 응답 생성", description = "AI 추천 서버와 연동하여 추천곡/키워드/분위기 분석 결과 반환")
    @PostMapping("/ai-response")
    public AiResponseResponse generateAiResponse(@RequestBody AiResponseRequest req) {
        // ✅ Flask(Agent3 통합 파이프라인) 서버 호출
        return chatService.generateAiResponse(req);
    }

    @Operation(summary = "나의 순간 채팅 기록 조회", description = "특정 세션의 메시지 목록을 반환")
    @GetMapping("/history/my/{sessionId}")
    public ChatHistoryResponse getMyChatHistory(@PathVariable Long sessionId) {
        return chatService.getMyChatHistory(sessionId);
    }

    @Operation(summary = "나의 순간 메시지 목록 조회", description = "sessionId를 기준으로 모든 메시지를 반환")
    @GetMapping("/messages/my")
    public ChatHistoryResponse getMyChatMessages(@RequestParam Long sessionId) {
        return chatService.getMyChatHistory(sessionId);
    }

    @Operation(summary = "공간의 순간 메시지 목록 조회", description = "sessionId를 기준으로 모든 메시지를 반환")
    @GetMapping("/messages/space")
    public ChatHistoryResponse getSpaceChatMessages(@RequestParam Long sessionId) {
        return chatService.getSpaceChatHistory(sessionId);
    }
 
    @Operation(summary = "메시지 전송", description = "기존 세션 내 후속 채팅 입력 (가장 최근 세션만 가능)")
    @PostMapping("/message")
    public ChatMessageResponse sendMessage(@RequestBody ChatMessageRequest req) {
        return chatService.sendMessage(req);
    }

    // @Operation(summary = "세션 내 대화 요약 조회", description = "Gemini 기반으로 세션 전체를 요약하여 주요 내용/키워드/추천 결과를 반환")
    // @GetMapping("/summary/{sessionId}")
    // public ChatSummaryResponse getChatSummary(@PathVariable Long sessionId) {
    //     return chatService.getChatSummary(sessionId);
    // }


    // @Operation(summary = "채팅 내 검색", description = "모든 세션의 주제/입력 텍스트/AI 응답 중 키워드가 포함된 항목 반환")
    // @GetMapping("/search")
    // public List<ChatSearchResultResponse> searchChat(@RequestParam String keyword) {
    //     return chatService.searchChat(keyword);
    // }

    // @Operation(summary = "주변 음악 조회", description = "현재 위치 기준 주변 사용자의 재생 음악 목록 반환")
    // @GetMapping("/location/nearby-music")
    // public List<NearbyMusicResponse> getNearbyMusic(@RequestParam double lat, @RequestParam double lng) {
    //     return locationService.getNearbyMusic(lat, lng);
    // }

    // @Operation(summary = "주변 사용자 음악 상세 조회", description = "특정 사용자 ID로 현재 재생 음악 상세 조회")
    // @GetMapping("/location/nearby-music/{userId}")
    // public MusicDetailResponse getNearbyUserMusic(@PathVariable Long userId) {
    //     return locationService.getNearbyUserMusic(userId);
    // }

    // @Operation(summary = "음악 상세 조회", description = "곡 ID로 가사, 앨범, 미리듣기, 가수 등 세부정보 반환")
    // @GetMapping("/music/{songId}")
    // public MusicDetailResponse getMusicDetail(@PathVariable Long songId) {
    //     return musicService.getMusicDetail(songId);
    // }
    
    // @Operation(summary = "AI 추천 재생목록 전체 보기", description = "세션 ID 기반으로 AI 추천곡 전체 반환")
    // @GetMapping("/player/playlist")
    // public PlaylistResponse getPlaylist(@RequestParam Long sessionId) {
    //     return playerService.getPlaylist(sessionId);
    // }

    // @Operation(summary = "AI 추천 재생목록 저장", description = "추천받은 곡들을 세션별로 저장")
    // @PostMapping("/player/playlist/save")
    // public ApiResponse savePlaylist(@RequestBody PlaylistSaveRequest req) {
    //     return playerService.savePlaylist(req);
    // }
}

