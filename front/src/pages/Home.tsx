import { useState } from "react";
import DefaultHeader from "../components/DefaultHeader";

// src/pages/Home.tsx
interface HomeProps {
  setShowOverlay: (v: boolean) => void;
}

export default function Home({ setShowOverlay }: HomeProps) {
  const [showSheet, setShowSheet] = useState(false);

  const openSheet = () => {
    setShowSheet(true);
    setShowOverlay(true);  // 전역 오버레이 ON
  };

  const closeSheet = () => {
    setShowSheet(false);
    setShowOverlay(false); // 전역 오버레이 OFF
  };
  
  return (
    <div>
        <DefaultHeader/>
      <h1>홈 페이지</h1>
      <button onClick={openSheet} style={{position: "relative", zIndex: 1000}}>사진 추가하기</button>
      <button onClick={closeSheet} style={{position: "relative", zIndex: 1000}}>사진 닫기</button>
    </div>
  );
}