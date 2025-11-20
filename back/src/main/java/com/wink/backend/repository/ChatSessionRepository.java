package com.wink.backend.repository;

import com.wink.backend.entity.ChatSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface ChatSessionRepository extends JpaRepository<ChatSession, Long> {

    // 전체 세션 목록 (타입별, 최신순)
    List<ChatSession> findByTypeOrderByStartTimeDesc(String type);

    // 타입별 최신 세션 (진행/종료 상관 없음)
    Optional<ChatSession> findTopByTypeOrderByStartTimeDesc(String type);

    // 진행 중인(isEnded=false) 세션들만 (안전용)
    List<ChatSession> findByTypeAndIsEndedFalseOrderByStartTimeDesc(String type);

    Optional<ChatSession> findTopByTypeAndIsEndedFalseOrderByStartTimeDesc(String type);

    List<ChatSession> findByTypeAndIsEndedTrueOrderByStartTimeDesc(String type);

}
