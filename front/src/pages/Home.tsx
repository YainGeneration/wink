import { useState } from "react";
import DefaultHeader from "../components/DefaultHeader";
import S from '../styles/styled.ts'
import theme from "../styles/theme.ts";

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
    <S.Padding16px>
        <div style={{marginLeft: "14px"}}>
          <div>
            <S.Heading2>지금 어떤 순간을</S.Heading2>
            <S.Heading2>음악으로 담고 싶으신가요?</S.Heading2>
          </div>
          <div style={{marginTop: "16px", marginBottom: "36px"}}>
            <S.Heading3>감정, 하고 있는 일 등을 말해주시면</S.Heading3>
            <S.Heading3>어울리는 음악을 찾아드릴게요</S.Heading3>
          </div>
          <div>
            <button style={{
              marginBottom: "8px",
              backgroundColor: theme.colors.grayscale.g50,
              border: `1px solid ${theme.colors.grayscale.g100}`,
              padding: "4px 16px",
              borderRadius: "16px",
              width: "fit"
              }}>
                <S.Body2>출근길 기분 좋아지는 여성 보컬 팝 추천해줘</S.Body2>
            </button>
            <button style={{
              marginBottom: "8px",
              backgroundColor: theme.colors.grayscale.g50,
              border: `1px solid ${theme.colors.grayscale.g100}`,
              padding: "4px 16px",
              borderRadius: "16px",
              width: "fit"
              }}>
                <S.Body2>비오는 날 어울리는 차분한 R&B 알려줘</S.Body2>
            </button>
            <button style={{
              marginBottom: "8px",
              backgroundColor: theme.colors.grayscale.g50,
              border: `1px solid ${theme.colors.grayscale.g100}`,
              padding: "4px 16px",
              borderRadius: "16px",
              width: "fit"
              }}>
                <S.Body2>책 읽을 때 어울리는 재즈 플레이리스트 보여줘</S.Body2>
            </button>
          </div>
        </div>
      
      
      {/* <button onClick={openSheet} style={{position: "relative", zIndex: 1000}}>사진 추가하기</button>
      <button onClick={closeSheet} style={{position: "relative", zIndex: 1000}}>사진 닫기</button> */}
    </S.Padding16px>
  );
}