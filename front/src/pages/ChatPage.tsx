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


const mockChatMessages: ChatSession[] = [
    {
      sessionId: 76,
      type: "MY",
      topic: "황혼녘 겨울, 차가운 바람결에 실려온 멜로디",
      latest: true,
      messages: [
        {
        messageId: 134,
        sessionId: 76,
        sender: "user",
        text: "추운 겨울 해가 지고 있을 때 듣기 좋은 노래 추천해줘",
        imageBase64: [
            "http://localhost:8080/chat-images/1763659121084_0baf9e08-8110-4e89-81ac-4795c0ac8cff.jpg"
        ],
        keywords: [],
        recommendations: [],
        mergedSentence: null,
        interpretedSentence: null,
        timestamp: "2025-11-21T02:18:41"
        },
        {
        messageId: 135,
        sessionId: 76,
        sender: "ai",
        text: "요청하신 음악 추천 결과입니다.",
        imageBase64: null,
        keywords: [
            "도시 풍경",
            "잔잔한",
            "고요한",
            "포근한",
            "따스한"
        ],
        recommendations: [
            {
            songId: "track_1257047",
            title: "Morning",
            artist: "Ithar",
            albumCover: "https://picsum.photos/200/200?random=4872",
            previewUrl: "https://storage.mp3-jamendo.com/download.php?trackid=1257047&format=mp3",
            durationMs: 77500,
            durationFormatted: "01분 17초",
            spotifyEmbedUrl: null,
            trackUrl: "http://www.jamendo.com/track/1257047"
            },
            {
            songId: "track_0645217",
            title: "Searching for balance",
            artist: "Libra Makowski",
            albumCover: "https://picsum.photos/200/200?random=1715",
            previewUrl: "https://storage.mp3-jamendo.com/download.php?trackid=645217&format=mp3",
            durationMs: 273100,
            durationFormatted: "04분 33초",
            spotifyEmbedUrl: null,
            trackUrl: "http://www.jamendo.com/track/645217"
            },
            {
            songId: "track_1109811",
            title: "Ocean of Faces ",
            artist: "Udo Vismann",
            albumCover: "https://picsum.photos/200/200?random=3234",
            previewUrl: "https://storage.mp3-jamendo.com/download.php?trackid=1109811&format=mp3",
            durationMs: 147600,
            durationFormatted: "02분 27초",
            spotifyEmbedUrl: null,
            trackUrl: "http://www.jamendo.com/track/1109811"
            }
        ],
        mergedSentence: "A calm and serene song to accompany the warm, fading glow of a winter sunset over a sprawling, quiet cityscape.",
        interpretedSentence: "고요한 겨울 도시를 물들이는 석양 아래, 따스하게 스며드는 이 노래. 차분한 마음으로 하루를 마무리하고 싶을 때, 이 노래를 들어보시라고 추천합니다.\n",
        timestamp: "2025-11-21T02:18:51"
        }
    ]
    }
];


export default function ChatPage() {
  // 백엔드 연동용 state는 일단 놔두되,
  // 지금 렌더링에는 사용하지 않아도 됨
  const { sessionId } = useParams();
  const location = useLocation();

  // 이 ChatMessage 타입도 UserChat에서 가져온 타입임
  const [messages, setMessages] = useState<ChatMessage[]>([]);

  // mockOldChatMessages를 flatten해서 ChatMessage[]로 만든다
  // 1. mockOldChatMessages 테스트
  // const flattenedMessages: ChatMessage[] = mockOldChatMessages.flatMap(
  //   (s) => s.messages ?? []
  // );
  // 2. mockNewChatMessages 테스트
  // const flattenedMessages: ChatMessage[] = mockNewChatMessages.flatMap(
  //   (s) => s.messages ?? []
  // );


  // effect는 나중에 연동할 때 다시 손보자 (지금은 주석 처리해도 됨)
  /*
  useEffect(() => {
    async function loadChat() {
      if (!sessionId) return;

      const res = await fetch(`/api/chat/${sessionId}/summary`);
      const data = await res.json();

      if (data.latest === true) {
        const chatMsgs: ChatMessage[] = convertSummaryToMessages(data.summaryMode);
        setMessages(chatMsgs);
      }
    }

    if (location.state?.newChatData) {
      const newMsgs: ChatMessage[] = convertNewChatToMessages(location.state.newChatData);
      setMessages(newMsgs);
      return;
    }

    loadChat();
  }, [sessionId, location.state]);
  */

  return (
    <S.Padding16px>
       {/* 나중에는 messages를 넘길 거고, 지금은 mock으로 테스트 */}
      {/* <UserChat messages={flattenedMessages} /> */}
      <UserChat sessions={mockChatMessages} />
    </S.Padding16px>
    
  );
}
