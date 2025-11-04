// src/pages/ChatPage.tsx
import { useState } from "react";
import { useParams } from "react-router-dom";
import SystemChat from "../components/SystemChat"
import HistoryDrawer from "../components/HistoryDrawer";
import UserChat from "../components/UserChat";

export default function ChatPage() {
  const { sessionId } = useParams();
  const [isHistoryOpen, setIsHistoryOpen] = useState(false);

  return (
    <div className="relative w-full h-full">
      {/* 상단 바 */}
      <header className="flex items-center justify-between p-4">
        <button onClick={() => setIsHistoryOpen(true)}>📜 히스토리</button>
        <h1>세션 {sessionId}</h1>
      </header>

      {/* 채팅 영역 */}
      <UserChat sessionId={sessionId!} />

      {/* 히스토리 Drawer (라우팅 아님) */}
      {isHistoryOpen && (
        <HistoryDrawer onClose={() => setIsHistoryOpen(false)} />
      )}
    </div>
  );
}
