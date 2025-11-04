// src/AppRoutes.tsx
import { Routes, Route, Navigate } from "react-router-dom";
import Home from "./pages/Home";
import ChatPage from "./pages/ChatPage";
import HistoryDetail from "./pages/HistoryDetail"; // optional

const AppRoutes = () => {
  return (
    <Routes>
      {/* 새 채팅 시작 (홈) */}
      <Route path="/home" element={<Home />} />

      {/* 채팅 진행 페이지 */}
      <Route path="/chat/:sessionId" element={<ChatPage />} />

      {/* (선택) 과거 대화 보기 */}
      <Route path="/history/:id" element={<HistoryDetail />} />

      {/* 기본 진입 시 홈으로 리디렉트 */}
      <Route path="*" element={<Navigate to="/home" replace />} />
    </Routes>
  );
};

export default AppRoutes;