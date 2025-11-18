// src/AppRoutes.tsx
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import ChatPage from "./pages/ChatPage";
import HistoryDetail from "./pages/HistoryDetail"; // optional
import NearBy from "./pages/NearBy";
import Recommend from "./pages/Recommend";
import Story from "./pages/Story";
import My from "./pages/My";

interface AppRoutesProps {
  setShowOverlay: (v: boolean) => void;
}

const AppRoutes = ({ setShowOverlay }: AppRoutesProps) => {
  return (
    <Routes>
      {/* 새 채팅 시작 (홈) */}
      <Route path="/home" element={<Home setShowOverlay={setShowOverlay}/>} />

      {/* 채팅 진행 페이지 */}
      <Route path="/chat/:sessionId" element={<ChatPage />} />

      {/* (선택) 과거 대화 보기 */}
      <Route path="/history/:id" element={<HistoryDetail />} />

      {/* 주변 */}
      <Route path="/nearby" element={<NearBy />} />

      {/* 추천 */}
      <Route path="/recommend" element={<Recommend />} />

      {/* 스토리 */}
      <Route path="/story" element={<Story />} />

      {/* My */}
      <Route path="/my" element={<My />} />

      {/* 기본 진입 시 홈으로 리디렉트 */}
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  );
};

export default AppRoutes;