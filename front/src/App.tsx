import { BrowserRouter } from "react-router-dom";
import { ThemeProvider } from "styled-components";
import { AnimatePresence } from "framer-motion";
import GlobalStyle from "./styles/GlobalStyle";
import theme from "./styles/theme";
// import AppRoutes from "./AppRoutes";
import WinkSplash from "./pages/WinkSplash";
import { useState } from "react";
import AppLayout from "./layouts/AppLayout";

function App(){
  const [showSplash, setShowSplash] = useState(true);

  return (
    <BrowserRouter>
      <ThemeProvider theme={theme}>
        <GlobalStyle />

        {/* 🧱 AppLayout으로 모든 페이지 감싸기 */}
        <AppLayout>
          {/* <AppRoutes /> */}

          <AnimatePresence>
            {showSplash && (
              <WinkSplash
                onDone={() => setShowSplash(false)}
                stepMs={1200}
              />
            )}
          </AnimatePresence>
        </AppLayout>
      </ThemeProvider>
    </BrowserRouter>
  );
}

export default App
