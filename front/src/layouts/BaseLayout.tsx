import styled, { useTheme } from "styled-components";
import statusBar from "../assets/ui/StatusBar.svg";
import homeIndicator from "../assets/ui/HomeIndicator.svg";
import theme from "../styles/theme";
import S from "../styles/styled.ts";
import pause from "../assets/icons/player-pause.svg";
import play from "../assets/icons/player-play.svg";
import back from "../assets/icons/player-skip-back.svg";
import forward from "../assets/icons/player-skip-forward.svg";
import playlist from "../assets/icons/playlist.svg";
import wave from "../assets/icons/wave.svg";
import upWhite from "../assets/icons/arrow-up-white.svg";
import home from "../assets/icons/home.svg"
import homeFill from "../assets/icons/home-fill.svg"
import homeWhite from "../assets/icons/home-white.svg"
import mapPin from "../assets/icons/map-pin.svg"
import mapPinFill from "../assets/icons/map-pin-fill.svg"
import mapPinWhite from "../assets/icons/map-pin-white.svg"
import headphones from "../assets/icons/headphones.svg"
import headphonesFill from "../assets/icons/headphones-fill.svg"
import headphonesWhite from "../assets/icons/headphones-white.svg"
import dashboard from "../assets/icons/layout-dashboard.svg"
import dashboardFill from "../assets/icons/layout-dashboard-fill.svg"
import dashboardWhite from "../assets/icons/layout-dashboard-white.svg"
import user from "../assets/icons/user.svg"
import userFill from "../assets/icons/user-fill.svg"
import userWhite from "../assets/icons/user-white.svg"
import plusG600 from "../assets/icons/plus-g600.svg"
import { useLocation, useNavigate } from "react-router-dom"; 
import { useRef, useState, useEffect, useMemo } from "react";
import { useMusicPlayer } from "../components/MusicPlayerContext.tsx";
import PlayBar from "../components/PlayBar";
import DefaultHeader from "../components/DefaultHeader.tsx";
import SideBar from "../components/SideBar.tsx";
import AddPhoto from "../components/BottomSheet/AddPhoto.tsx";

type Props = {
  children: React.ReactNode;
  showOverlay?: boolean;
  backgroundColor?: string;
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
  height: max-content;
  padding: 8px 4px 8px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;

  width: 90%;
  background-color: ${theme.colors.white};
  border: 1px solid ${theme.colors.grayscale.g100};
  position: absolute;
  bottom: 170px;
  border-radius: 22px;
  box-shadow: ${theme.shadow.default};

   & input {
    border: none;
    outline: none;
  }

  & .imgWrapper {
    display: flex;
    width: 100%;
    justify-content: flex-start;
    padding: 0px 16px 16px;
    gap: 8px;

    & img {
      width: 114px;
      height: 114px;
      border-radius: 10px;
      margin-top: 6px;
    }
  }
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

// 추가: 탭바 (하단 navigation)
const TabBar = styled.div<{ isRecommend: boolean }>`
  height: 78px;

  display: flex;
  justify-content: space-around;
  align-items: flex-start;
  padding-top: 10px;

  background-color: ${({ isRecommend }) =>
    isRecommend ? "rgba(255, 255, 255, 0.2)" : theme.colors.white};
  backdrop-filter: ${({ isRecommend }) =>
    isRecommend ? "blur(30px)" : "none"};
`;

// 탭 설정 배열
const tabs = [
  {
    path: "/home",
    label: "홈",
    icon: home,
    iconFill: homeFill,
    iconWhite: homeWhite
  },
  {
    path: "/nearby",
    label: "주변",
    icon: mapPin,
    iconFill: mapPinFill,
    iconWhite: mapPinWhite
  },
  {
    path: "/recommend",
    label: "추천",
    icon: headphones,
    iconFill: headphonesFill,
    iconWhite: headphonesWhite
  },
  {
    path: "/story",
    label: "스토리",
    icon: dashboard,
    iconFill: dashboardFill,
    iconWhite: dashboardWhite
  },
  {
    path: "/my",
    label: "My",
    icon: user,
    iconFill: userFill,
    iconWhite: userWhite
  }
];


export default function BaseLayout({ children, showOverlay, backgroundColor }: Props) {
  const location = useLocation();
  const navigate = useNavigate();
  const theme = useTheme();
  const { currentTrack } = useMusicPlayer();
  console.log(currentTrack)
  
  const currentPath = location.pathname;
  const showPlayBar = currentPath === "/home";
  const isRecommend = location.pathname === "/recommend";

  const prefixes = ['/home', '/chat'];
  const isChatMatch = useMemo(() => {
    return prefixes.some(prefix => currentPath.startsWith(prefix));
  }, [currentPath, prefixes]);

  // Header 숨겨야 하는 경로 prefix
  const hideHeaderPrefixes = ["/nearby", "/recommend", "/story", "/my"];
  // 현재 경로가 위 3개 중 하나로 시작하면 Header 숨김
  const hideHeader = hideHeaderPrefixes.some(prefix =>
    location.pathname.startsWith(prefix)
  );

  const [isSideBarOpen, setSideBarOpen] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [addPhotoSheetOpen, setAddPhotoSheetOpen] = useState(false);




  // 오디오 재생 진행바 상태
  const audioRef = useRef<HTMLAudioElement | null>(null);


   // currentTrack 변경될 때마다 audio 재생
  useEffect(() => {
    if (audioRef.current) {
      audioRef.current.load(); // 재생 준비까지만
      // audioRef.current.play(); // 자동재생 막기
    }
  }, [currentTrack]);


  return (
    <>
      
      <LayoutWrap backgroundColor={backgroundColor}>
        <SideBar open={isSideBarOpen} onClose={() => setSideBarOpen(false)} />
        <AddPhoto
          open={addPhotoSheetOpen}
          onClose={() => setAddPhotoSheetOpen(false)}
          onSelect={(img) => setSelectedImage(img)} // 선택한 이미지들 세팅
        />

        <TopBar>
          <img src={statusBar} alt="statusBar" />
        </TopBar>

        {!hideHeader && (
          <div style={{ width: "100%", padding: "16px" }}>
            <DefaultHeader onMenuClick={() => setSideBarOpen(true)} />
          </div>
        )}

        <Content>{children}</Content>

        { isChatMatch && (
          <ChatInput>
            {/* 
            이 자리에 아래와 같이 AddPhoto에서 선택한 사진들이 들어가야 함
            <div className="imgWrapper">
              <img src="https://picsum.photos/id/1011/200/200" alt="" />
            </div> */}

            {/* 선택된 이미지가 있으면 표시 */}
            {selectedImage && (
              <div className="imgWrapper"
                style={{
                  // width: "100px",
                  // overflow: "hidden",
                  // borderRadius: "6px",
                  // marginLeft: "8px"
                  // display: flex;
                  // width: 100%;
                  // justify-content: flex-start;
                  // padding: 0px 16px 16px;
                }}
              >
                <img
                  src={selectedImage}
                  alt=""
                />
              </div>
            )}



            <div
              style={{
                display: "flex",
                flexDirection: "row",
                alignItems: "center",
                width: "100%"
              }}  
            >
                {/* 현재 재생중인 앨범 커버 */}
                <div
                  style={{
                    width: "30px",
                    height: "30px",
                    position: "relative",
                    overflow: "hidden",
                    borderRadius: "20px",
                    marginLeft: "8px",
                  }}
                >
                  <img 
                    src={currentTrack.image} alt="" 
                    style={{ 
                      width: "100%", 
                      height: "100%", 
                      objectFit: "cover",
                      filter: "brightness(80%)",
                    }}
                  />
                  <img
                    src={wave}
                    alt="icon"
                    style={{
                      position: "absolute",
                      top: "50%",
                      left: "50%",
                      transform: "translate(-50%, -50%)",
                    }}
                  />
                </div>
                <button
                  style={{
                    width: "30px",
                    height: "30px",
                    overflow: "hidden",
                    borderRadius: "20px",
                    marginLeft: "6px",
                    alignItems: "center",
                    justifyContent: "center",
                    display: "flex",
                    border: `1px solid ${theme.colors.grayscale.g100}`
                  }}
                  
                  onClick={() => setAddPhotoSheetOpen(true)}
                >
                  <img src={plusG600} alt="" 
                    style={{ width: "24px", height: "24px", objectFit: "cover" }}
                  />
                </button>
                <input type="text" 
                  style={{flex: "1", marginLeft: "10px"}}
                  placeholder="오늘은 어떤 노래를 들어볼까요?"/>
                <button
                  style={{
                    width: "30px",
                    height: "30px",
                    overflow: "hidden",
                    borderRadius: "20px",
                    marginRight: "10px",
                    alignItems: "center",
                    justifyContent: "center",
                    display: "flex",
                    backgroundColor: theme.colors.primary,
                  }}
                >
                  <img src={upWhite} alt="" 
                    style={{ width: "24px", height: "24px", objectFit: "cover" }}
                  />
                </button>
            </div>
            
          </ChatInput>
        )}

        <BottomPlayerArea>
          {showPlayBar && <PlayBar />}
        
          <TabBar isRecommend={isRecommend}>
            {tabs.map((tab) => {
              const isActive = location.pathname === tab.path;

              // 아이콘 조건
              const iconToShow = isRecommend
                ? tab.iconWhite         // recommend 페이지 → 무조건 흰색 아이콘
                : isActive
                ? tab.iconFill          // 일반 페이지 active
                : tab.icon;             // 일반 페이지 inactive

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
                  {/* <img 
                    src={isActive ? tab.iconFill : tab.icon}  // 아이콘 Fill 적용
                    alt={tab.label}
                  /> */}
                  <img src={iconToShow} alt={tab.label} />
                  <S.Smalltext
                    style={{
                      color: isRecommend
                        ? theme.colors.white                // ✔ recommend page → white text
                        : isActive
                        ? theme.colors.black
                        : theme.colors.grayscale.g500,
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
    </>
    
  );
}
