// src/layouts/AppLayout.tsx
import styled from "styled-components";
import statusBar from "../assets/ui/StatusBar.svg";
import homeIndicator from "../assets/ui/HomeIndicator.svg";
import theme from "../styles/theme";
import S from "../styles/styled.ts";
import pause from "../assets/icons/player-pause.svg";
import back from "../assets/icons/player-skip-back.svg";
import forward from "../assets/icons/player-skip-forward.svg";
import playlist from "../assets/icons/playlist.svg";
import home from "../assets/icons/home.svg"
import homeFill from "../assets/icons/home-fill.svg"
import mapPin from "../assets/icons/map-pin.svg"
import mapPinFill from "../assets/icons/map-pin-fill.svg"
import headphones from "../assets/icons/headphones.svg"
import headphonesFill from "../assets/icons/headphones-fill.svg"
import dashboard from "../assets/icons/layout-dashboard.svg"
import dashboardFill from "../assets/icons/layout-dashboard-fill.svg"
import user from "../assets/icons/user.svg"
import userFill from "../assets/icons/user-fill.svg"
import sugarAlbumCover from "../assets/img/sugar_album_cover.jpg"
import { head } from "framer-motion/client";
import { useLocation, useNavigate } from "react-router-dom"; 
import { useMemo } from "react";


type Props = {
  children: React.ReactNode;
  showOverlay?: boolean;
};


const LayoutWrap = styled.div<{ backgroundColor?: string }>`
  position: relative;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  align-items: center;
  box-sizing: border-box;
  height: 100%;
  width: 100%;
  background-color: ${({ backgroundColor }) => backgroundColor || "#fff"};
`;

const Content = styled.main`
  width: 100%;
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
  pointer-events: none;
  z-index: 4;
`;

const Overlay = styled.div`
  position: absolute;
  inset: 0;
  background-color: rgba(18, 18, 18, 0.03); /* 반투명 어두운 효과 */
  z-index: 6;
  pointer-events: auto;
`;

// 채팅 입력바
const ChatInput = styled.div`
  width: 90%;
  background-color: ${theme.colors.white};
  border: 1px solid ${theme.colors.grayscale.g100};
  height: 46px;
  position: absolute;
  bottom: 170px;
  border-radius: 23px;
  box-shadow: ${theme.shadow.default};
  display: flex;
  flex-direction: row;
  justify-content: space-around;
`

// 플레이바 + 탭바 감싸는 Wrapper
const BottomPlayerArea = styled.div`
  position: absolute;
  bottom: 0px;
  left: 0;
  right: 0;
  width: 100%;

  display: flex;
  flex-direction: column;

  z-index: 3; /* Overlay보다 위에 있어야 함 */
  pointer-events: auto;
  box-shadow: ${theme.shadow.default}
`;

// 추가: 음악 플레이바
const PlayerBar = styled.div`
  height: 76px;
  padding: 8px 16px 0px;

  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-direction: column;

  background-color: ${({ theme }) => theme.colors.white};
`;

// 추가: 탭바 (하단 navigation)
const TabBar = styled.div`
  height: 78px;

  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  padding-top: 10px;

  background-color: ${({ theme }) => theme.colors.white};
`;

// 탭 설정 배열
const tabs = [
  {
    path: "/home",
    label: "홈",
    icon: home,
    iconFill: homeFill
  },
  {
    path: "/nearby",
    label: "주변",
    icon: mapPin,
    iconFill: mapPinFill
  },
  {
    path: "/recommend",
    label: "추천",
    icon: headphones,
    iconFill: headphonesFill
  },
  {
    path: "/story",
    label: "스토리",
    icon: dashboard,
    iconFill: dashboardFill
  },
  {
    path: "/my",
    label: "My",
    icon: user,
    iconFill: userFill
  }
];


export default function BaseLayout({ children, showOverlay }: Props) {
  const location = useLocation();
  const currentPath = location.pathname;

  const isHomePage = currentPath === "/home";

  const prefixes = ['/home', '/chat'];
  const isMatch = useMemo(() => {
    return prefixes.some(prefix => currentPath.startsWith(prefix));
  }, [currentPath, prefixes]);

  const navigate = useNavigate();
  
  return (
    <LayoutWrap>
      <TopBar>
        <img src={statusBar} alt="statusBar" />
      </TopBar>

      <Content>{children}</Content>

      { isMatch && (
        <ChatInput>
          <div>
            <img src="" alt="" />
          </div>
          <button></button>
          <input type="text" />
          <button></button>
        </ChatInput>
      )}

      <BottomPlayerArea>
        {isHomePage && (
          <PlayerBar>
            <div
              style={{
                display: "flex",
                alignContent: "center",
                justifyContent: "space-between",
                width: "100%",
                paddingTop: "10px"
              }}
            >
              <div>
                {/* <img src="" alt="" /> */}
                <div
                  style={{
                    display: "flex",
                    flexDirection: "row",
                    gap: "11px",
                    
                  }}
                >
                  <div
                    style={{
                      width: "40px",
                      height: "40px",
                      borderRadius: "4px",
                      overflow: "hidden"
                    }}
                  >
                    <img src={sugarAlbumCover} alt=""
                      style={{
                        width: "100%",
                        height: "100%",
                        objectFit: "cover"
                      }}
                    />
                  </div>
                  <div
                    style={{
                      display: "flex",
                      flexDirection: "column",
                      justifyContent: "center"
                    }}
                  >
                    <S.Caption>노래 제목</S.Caption>
                    <div style={{color: theme.colors.grayscale.g600}}>
                      <S.Smalltext>가수 이름</S.Smalltext>
                    </div>
                  </div>
                  
                </div>
              </div>
              <div
                style={{
                  display: "flex",
                  flexDirection: "row",
                  gap: "12px",
                  alignContent: "center",
                  flexWrap: "unset"
                }}
              >
                <button><img src={back} alt="" /></button>
                <button><img src={pause} alt="" /></button>
                <button><img src={forward} alt="" /></button>
                <button><img src={playlist} alt="" /></button>
              </div>
            </div>

            {/* 재생바 */}
            <div>
              
            </div>
          </PlayerBar>
        )}
        

        <TabBar>
          {tabs.map((tab) => {
            const isActive = location.pathname === tab.path;

            return (
              <button
                key={tab.path}
                onClick={() => navigate(tab.path)}   // 클릭 시 라우팅
                style={{
                  display: "flex",
                  flexDirection: "column",
                  alignItems: "center",
                  gap: "2px",
                  background: "none",
                  border: "none",
                }}
              >
                <img 
                  src={isActive ? tab.iconFill : tab.icon}  // 아이콘 Fill 적용
                  alt={tab.label}
                />
                <S.Smalltext
                  style={{
                    color: isActive
                      ? theme.colors.black
                      : theme.colors.grayscale.g500
                  }}
                >
                  {tab.label}
                </S.Smalltext>
              </button>
            );
          })}
        </TabBar>

      </BottomPlayerArea>

      <BottomBar>
        <img src={homeIndicator} alt="homeIndicator" />
      </BottomBar>

      {showOverlay && <Overlay />}
    </LayoutWrap>
  );
}
