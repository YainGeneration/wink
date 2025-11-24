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

  if (location.state?.newChatData) {
    const newSession: ChatSession = {
      sessionId: Number(sessionId),
      type: "MY",
      topic: location.state.newChatData.topic,
      messages: convertNewChatToMessages(location.state.newChatData),
    };
    setSessions([newSession]);
    return;
  }

  loadChat();
}, [sessionId, location.state]);


  console.log(`넘겨줌: `, sessions);

  return (
    <S.Padding16px>
      <UserChat sessions={sessions} />
    </S.Padding16px>
    
  );
}
