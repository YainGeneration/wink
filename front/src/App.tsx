import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "styled-components";
import { AnimatePresence } from "framer-motion";
import GlobalStyle from "./styles/GlobalStyle";
import theme from "./styles/theme";
import AppRoutes from "./AppRoutes";
import WinkSplash from "./pages/WinkSplash";
import { useEffect, useState } from "react";
import AppLayout from "./layouts/AppLayout";

function App(){
  const [showSplash, setShowSplash] = useState(false);
  const [isChecking, setIsChecking] = useState(true);

  useEffect(() => {
    // 스플래시를 이미 본 적 있는지 확인
    const splashSeen = sessionStorage.getItem("splashSeen");

    if (!splashSeen) {
      setShowSplash(true);
      // 2~3초 후 스플래시 닫기
      setTimeout(() => {
        setShowSplash(false);
        sessionStorage.setItem("splashSeen", "true");
        setIsChecking(false);
      }, 3000); // 스플래시 지속 시간
    } else {
      // 이미 본 적 있다면 바로 false로
      setShowSplash(false);
      setIsChecking(false);
    }
  }, []);

  return (
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <GlobalStyle />
        {/* AppLayout으로 모든 페이지 감싸기 */}
        <AppLayout>
          <AppRoutes />
          <AnimatePresence>
            {showSplash ? (
              <WinkSplash
                onDone={() => setShowSplash(false)}
                stepMs={1200}
              />
            ) : (<AppRoutes />
            )}
          </AnimatePresence>
        </AppLayout>
      </ThemeProvider>
    </BrowserRouter>
  );
}

export default App
