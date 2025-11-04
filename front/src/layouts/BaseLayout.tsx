// src/layouts/AppLayout.tsx
import styled from "styled-components";
import statusBar from "../assets/ui/StatusBar.svg";
import homeIndicator from "../assets/ui/HomeIndicator.svg";


type Props = {
  children: React.ReactNode;
  showOverlay?: boolean;
};


const LayoutWrap = styled.div<{ backgroundColor?: string }>`
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  padding: 0px 16px;
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  background-color: ${({ backgroundColor }) => backgroundColor || "#fff"};
`;

const Content = styled.main`
//   flex: 1;
//   display: flex;
  width: 100%;
//   flex-direction: column;
//   justify-content: center;
  padding: 58px 0px 0px;
  height: 100%
`;

const TopBar = styled.div`
  position: absolute;
  top: 8px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  pointer-events: none;
`;

const BottomBar = styled.div`
  position: absolute;
  bottom: 8px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  pointer-events: none;
`;

const Overlay = styled.div`
  position: absolute;
  inset: 0;
  background-color: rgba(18, 18, 18, 0.03); /* 반투명 어두운 효과 */
  z-index: 2;
  pointer-events: auto;
`;

export default function BaseLayout({ children, showOverlay }: Props) {
  return (
    <LayoutWrap>
      <TopBar>
        <img src={statusBar} alt="statusBar" />
      </TopBar>

      <Content>{children}</Content>

      <BottomBar>
        <img src={homeIndicator} alt="homeIndicator" />
      </BottomBar>

      {showOverlay && <Overlay />}
    </LayoutWrap>
  );
}
