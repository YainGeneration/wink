import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "styled-components";
import { AnimatePresence } from "framer-motion";
import GlobalStyle from "./styles/GlobalStyle";
import theme from "./styles/theme";
import AppRoutes from "./AppRoutes";
import WinkSplash from "./pages/WinkSplash";
import { useEffect, useState } from "react";
import AppLayout from "./layouts/AppLayout";
import BaseLayout from "./layouts/BaseLayout";

function App(){
  const [showSplash, setShowSplash] = useState(false);
  const [isChecking, setIsChecking] = useState(true);
  const [showOverlay, setShowOverlay] = useState(false);

  useEffect(() => {
    // 스플래시를 이미 본 적 있는지 확인
    const splashSeen = sessionStorage.getItem("splashSeen");

    if (!splashSeen) {
      setShowSplash(true);
      // 2~3초 후 스플래시 닫기
      const timer = setTimeout(() => {
        setShowSplash(false);
        sessionStorage.setItem("splashSeen", "true");
        setIsChecking(false);
      }, 5000);
      return () => clearTimeout(timer);
    } else {
      // 이미 본 적 있다면 바로 false로
      setShowSplash(false);
      setIsChecking(false);
    }
  }, []);

    if (isChecking) return null;

  return (
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <GlobalStyle />
        {/* AppLayout으로 모든 페이지 감싸기 */}
        <AppLayout backgroundColor="#fff">
          {/* ✅ BaseLayout: StatusBar, HomeIndicator, Overlay 포함 */}
          <BaseLayout showOverlay={showOverlay}>
            <AnimatePresence mode="wait">
              {showSplash ? (
                <WinkSplash onDone={() => setShowSplash(false)} stepMs={1200} />
              ) : (
                <AppRoutes setShowOverlay={setShowOverlay} />
              )}
            </AnimatePresence>
          </BaseLayout>
        </AppLayout>
      </ThemeProvider>
    </BrowserRouter>
  );
}

export default App;
