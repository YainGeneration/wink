// src/pages/ChatPage.tsx
import { useEffect, useState } from "react";
import { useLocation, useParams } from "react-router-dom";
import SystemChat from "../components/SystemChat"
import HistoryDrawer from "../components/HistoryDrawer";
import S from "../styles/styled";
import styled from "styled-components";
import theme from "../styles/theme";
import { convertSummaryToMessages } from "../utils/convertSummaryToMessages";
import { convertNewChatToMessages } from "../utils/convertNewChatToMessages";

import UserChat, {
  type ChatSession,
  type ChatMessage,
} from "../components/UserChat";



export default function ChatPage() {
  // 백엔드 연동용 state는 일단 놔두되,
  // 지금 렌더링에는 사용하지 않아도 됨
  const { sessionId } = useParams();
  const location = useLocation();

  // 이 ChatMessage 타입도 UserChat에서 가져온 타입임
  const [sessions, setSessions] = useState<ChatSession[]>([]);

  function appendMessage(newMsg: ChatMessage) {
  setSessions(prev => {
    if (prev.length === 0) return prev;

    const updated = [...prev];
    const target = updated[0];

    updated[0] = {
      ...target,
      messages: [...(target.messages ?? []), newMsg],
    };

    return updated;
  });
}

  // effect는 나중에 연동할 때 다시 손보자 (지금은 주석 처리해도 됨)
useEffect(() => {
  async function loadChat() {
    if (!sessionId) return;

    const res = await fetch(`http://localhost:8080/api/chat/${sessionId}/full`);
    const data = await res.json();

    console.log("로드된 채팅 데이터:", data);

    const session: ChatSession = {
      sessionId: data.sessionId,
      type: data.type,
      topic: data.topic,
      latest: data.latest,
      nearbyMusic: data.nearbyMusic,

      messages: data.messages.map((m: any): ChatMessage => ({
        messageId: m.messageId,
        sessionId: m.sessionId,
        sender: m.sender,
        text: m.text,
        imageBase64: m.imageBase64,
        keywords: m.keywords ?? [],
        recommendations: m.recommendations ?? [],
        mergedSentence: m.mergedSentence,
        interpretedSentence: m.interpretedSentence,
        englishText: m.englishText,
        englishCaption: m.englishCaption,
        imageDescriptionKo: m.imageDescriptionKo, // 혹시 들어오면 매핑
        timestamp: m.timestamp,
      }))
};

    setSessions([session]);
  }

  // 새로운 채팅 생성 (처음 들어올 때)
    if (location.state?.newChatData) {
      const newSession: ChatSession = {
        sessionId: Number(sessionId),
        type: "MY",
        topic: location.state.newChatData.topic,
        messages: location.state.newChatData.messages,
      };
      setSessions([newSession]);
      return;
    }

  loadChat();
}, [sessionId, location.state]);

 /* ----------------------- 후속 메시지 append ----------------------- */
  useEffect(() => {
    function handleNewMessage(e: any) {
      const data = e.detail;

      // messages 전체 중 마지막 메시지만 append
      const msgs = data.messages;
      if (!msgs || msgs.length === 0) return;

      const lastMsg = msgs[msgs.length - 1];

      const newMsg: ChatMessage = {
        messageId: lastMsg.messageId,
        sessionId: lastMsg.sessionId,
        sender: lastMsg.sender,
        text: lastMsg.text,
        imageBase64: lastMsg.imageBase64,
        keywords: lastMsg.keywords ?? [],
        recommendations: lastMsg.recommendations ?? [],
        mergedSentence: lastMsg.mergedSentence,
        interpretedSentence: lastMsg.interpretedSentence,
        englishText: lastMsg.englishText,
        englishCaption: lastMsg.englishCaption,
        imageDescriptionKo: lastMsg.imageDescriptionKo,
        timestamp: lastMsg.timestamp,
      };

      // 기존 세션에 append
      setSessions(prev => {
        if (prev.length === 0) return prev;
        const updated = [...prev];

        updated[0] = {
          ...updated[0],
          messages: [...(updated[0].messages ?? []), newMsg],
        };

        return updated;
      });
    }

    window.addEventListener("NEW_MESSAGE", handleNewMessage);
    return () => window.removeEventListener("NEW_MESSAGE", handleNewMessage);
  }, []);


  return (
    <S.Padding16px>
      {/* <UserChat sessions={sessions} onAddMessage={appendMessage}/> */}
      <UserChat sessions={sessions}/>
    </S.Padding16px>
    
  );
}
