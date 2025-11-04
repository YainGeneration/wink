import { useEffect, useState } from "react";
import { motion, AnimatePresence } from "framer-motion";
import styled, { useTheme } from "styled-components";
import symbol from "./../assets/logo/symbol.svg";
import wordmark from "./../assets/logo/wordmark.svg";

type Props = { onDone: () => void; stepMs?: number };

const WinkSplash = ({ onDone, stepMs = 1200 }: Props) => {
  const theme = useTheme();
  const [showWordmark, setShowWordmark] = useState(true);
  const [showSymbol, setShowSymbol] = useState(false);
  const [showCircle, setShowCircle] = useState(false);

  // 1. 일정 시간 뒤 워드마크 사라짐
  useEffect(() => {
    const t = setTimeout(() => setShowWordmark(false), stepMs);
    return () => clearTimeout(t);
  }, [stepMs]);

  // 2. 워드마크 사라지고 일정 딜레이 후 circle 확장, icon 중앙 생성
  useEffect(() => {
    if (!showWordmark) {
      const circleTimer = setTimeout(() => setShowCircle(true), 300);
      const symbolTimer = setTimeout(() => setShowSymbol(true), 1005);

      return () => {
        clearTimeout(circleTimer);
        clearTimeout(symbolTimer);
      };
    }
  }, [showWordmark]);

  // 3. 전체 Splash가 끝난 뒤 onDone() 호출
  useEffect(() => {
    const doneTimer = setTimeout(onDone, stepMs + 2500); // circle 확장 끝난 뒤 콜백
    return () => clearTimeout(doneTimer);
  }, [onDone, stepMs]);

  return (
    <SplashWrapper>
      <LogoWrapper>
        <AnimatePresence>
          {showWordmark && (
            <motion.img
              key="symbol"
              src={symbol}
              alt="symbol"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 1, x: 85}}
              transition={{ duration: 1, ease: "easeOut" }}
            />
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showWordmark && (
            <motion.img
              key="wordmark"
              src={wordmark}
              alt="wordmark"
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              exit={{ opacity: 0, x: 80 }}
              transition={{ duration: 1 }}
            />
          )}
        </AnimatePresence>

        <AnimatePresence>
          {showSymbol && (
            <motion.img
              key="symbol"
              src={symbol}
              alt="symbol"
              initial={{ opacity: 1 }}
              animate={{ opacity: 1}}
              transition={{ duration: 1.2 }}
            />
          )}
        </AnimatePresence>
      </LogoWrapper>

      {/* Circle 퍼짐 효과 */}
      <AnimatePresence>
        {showCircle && (
          <Circle
            key="circle"
            initial={{ scale: 0, opacity: 1, x: -70, y: -40 }}
            animate={{ scale: 50, opacity: 1 }}
            exit={{ opacity: 0 }}
            transition={{ duration: 5, ease: "easeInOut" }}
            $color={theme.colors.primary}
          />
        )}
      </AnimatePresence>
    </SplashWrapper>
  );
};

export default WinkSplash;

/* 스타일 정의 */

const SplashWrapper = styled.div`
  position: relative;
  width: 100%;
  height: 100vh;
  overflow: hidden; /* circle이 확장돼도 스크롤 안 생기게 */
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fff;
`;

const LogoWrapper = styled.div`
  display: flex;
  align-items: center;
  gap: 4px;
  z-index: 2;
`;

const Circle = styled(motion.div)<{ $color: string }>`
  position: absolute;
  top: 50%;
  left: 50%;
  width: 100px;
  height: 100px;
  background-color: ${({ $color }) => $color};
  border-radius: 50%;
  transform: translate(-50%, -50%);
  z-index: 1;
`;
