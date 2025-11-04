// components/HistoryDrawer.tsx
import styled from "styled-components";
import { motion } from "framer-motion";

const Drawer = styled(motion.div)`
  position: fixed;
  top: 0;
  left: 0;
  height: 100vh;
  width: 320px;
  background: white;
  box-shadow: 4px 0 12px rgba(0,0,0,0.1);
  overflow-y: auto;
`;

export default function HistoryDrawer({ onClose }: { onClose: () => void }) {
  const histories = [
    { id: "a1", title: "오늘의 대화", date: "2025-11-04" },
    { id: "a2", title: "지난주 상담", date: "2025-10-28" },
  ];

  return (
    <Drawer
      initial={{ x: -320 }}
      animate={{ x: 0 }}
      exit={{ x: -320 }}
      transition={{ duration: 0.4 }}
    >
      <button onClick={onClose}>닫기</button>
      <h2>히스토리</h2>
      <ul>
        {histories.map((h) => (
          <li key={h.id}>{h.title}</li>
        ))}
      </ul>
    </Drawer>
  );
}