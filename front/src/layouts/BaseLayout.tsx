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
import close from "../assets/icons/x-white.svg"
import { useLocation, useNavigate } from "react-router-dom"; 
import { useRef, useState, useEffect, useMemo } from "react";
import { useMusicPlayer } from "../components/MusicPlayerContext.tsx";
import PlayBar from "../components/PlayBar";
import DefaultHeader from "../components/DefaultHeader.tsx";
import SideBar from "../components/SideBar.tsx";
import AddPhoto from "../components/BottomSheet/AddPhoto.tsx";
import { style } from "framer-motion/client";

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
  // height: 100%;
  flex: 1;
  overflow-y: auto;
  padding-bottom: 70px; /* PlayBar/TabBar 공간 확보 */
`;

const TopBar = styled.div`
  position: absolute;
  top: 8px;
  left: 0;
  right: 0;
  display: flex;
  justify-content: center;
  pointer-events: none;
  z-index: 3;
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
`

const ImgWrapper = styled.div`
  position: relative;
  width: 114px;
  height: 114px;
  overflow: hidden;
  padding: 0px 0px 12px 12px;

    & .userImg {
      width: 100%;
      height: 100%;
      object-fit: cover;
      border-radius: 10px;
      margin-top: 6px;
    }

    & .closeBtn {
      position: absolute;
      top: 12px;
      right: 6px;
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
    isRecommend ? "blur(12px)" : "none"};
`;

const InputWrapper = styled.div`
  display: flex;
  flex-direction: row;
  align-items: center;
  width: 100%;
`;

const AlbumCover = styled.div`
  width: 30px;
  height: 30px;
  position: relative;
  overflow: hidden;
  border-radius: 20px;
  margin-left: 8px;

  & img.album {
    width: 100%;
    height: 100%; 
    object-fit: cover;
    filter: brightness(80%);
  }

  & img.wave {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
  }
`

const AddPhotoButton = styled.button`
    width: 30px;
    height: 30px;
    overflow: hidden;
    border-radius: 20px;
    margin-left: 6px;
    align-items: center;
    justify-content: center;
    display: flex;
    border: 1px solid ${theme.colors.grayscale.g100};

    & img {
      width: 24px;
      height: 24px;
      object-fit: cover;
    }
`

const SubmitButton = styled.button`
    width: 30px;
    height: 30px;
    overflow: hidden;
    border-radius: 20px;
    margin-right: 10px;
    align-items: center;
    justify-content: center;
    display: flex;
    background-color: ${theme.colors.primary};

    & img {
      width: 24px;
      height: 24px;
      object-fit: cover;
    }
`

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
  const { currentTrack, isPlaying } = useMusicPlayer();
  
  console.log(currentTrack)
  
  const currentPath = location.pathname;
  const showPlayBar = currentPath === "/home" || currentPath === "/chat";
  const isRecommend = location.pathname === "/recommend";

  const isChatPage = currentPath.startsWith("/chat/");
  const sessionId = isChatPage ? Number(currentPath.split("/chat/")[1]) : null;


  const [inputText, setInputText] = useState("");


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
  const [addPhotoSheetOpen, setAddPhotoSheetOpen] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [selectedImageBase64, setSelectedImageBase64] = useState<string | null>(null);



  // 오디오 재생 진행바 상태
  const audioRef = useRef<HTMLAudioElement | null>(null);


   // currentTrack 변경될 때마다 audio 재생
useEffect(() => {
  if (!audioRef.current) return;

  const audio = audioRef.current;

  // src를 강제로 재적용해야 브라우저가 새 파일로 인식함
  audio.src = currentTrack.audioUrl;

  audio.load();  // 파일 준비

  if (isPlaying) {
    audio.play().catch(err => {
      console.warn("자동재생 실패 (브라우저 정책):", err);
    });
  }
}, [currentTrack, isPlaying]);


  async function convertImageToBase64(imageUrl: string) {
    const res = await fetch(imageUrl);
    const blob = await res.blob();

    return new Promise<string>((resolve) => {
      const reader = new FileReader();
      reader.onloadend = () => {
        resolve(reader.result as string);
      };
      reader.readAsDataURL(blob);
    });
  }

async function handleChatSubmit() {
  try {
    if (!inputText && !selectedImageBase64) return;

    // 📌 1) 기존 세션에서 후속 채팅 입력
    if (isChatPage && sessionId) {
      const body = {
        sessionId: sessionId,
        text: inputText,
        imageBase64: selectedImageBase64 || null,
      };

      const res = await fetch("http://localhost:8080/api/chat/message", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
      });

      const data = await res.json();
      window.dispatchEvent(
          new CustomEvent("NEW_MESSAGE", { detail: data })
      );

      console.log("[기존 세션 후속 메시지 응답]", data);

      // 전송 후 입력 초기화
      setInputText("");
      setSelectedImage(null);
      return;
    }

    // 📌 2) 새 채팅 시작 (HOME에서 보냈을 때)
    const body = {
      type: "my",
      inputText: inputText,
      imageBase64: selectedImageBase64,
    };

    const res = await fetch("http://localhost:8080/api/chat/start/my", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body),
    });

    const data = await res.json();

    setInputText("");
    setSelectedImage(null);

    navigate(`/chat/${data.sessionId}`);
  } catch (e) {
    console.error("채팅 전송 실패:", e);
  }
}


  const handleSelectImage = async (url: string) => {
    setSelectedImage(url);

    const base64 = await convertImageToBase64(url);
    setSelectedImageBase64(base64);
  };


  return (
    <>
      
      <LayoutWrap backgroundColor={backgroundColor}>
        <SideBar open={isSideBarOpen} onClose={() => setSideBarOpen(false)} />
        

        <TopBar>
          <img src={statusBar} alt="statusBar" />
        </TopBar>

        {!hideHeader && (
          <div style={{ width: "100%", padding: "16px" }}>
            <DefaultHeader onMenuClick={() => setSideBarOpen(true)} />
          </div>
        )}

        <Content>
          {children}
          <AddPhoto
            open={addPhotoSheetOpen}
            onClose={() => setAddPhotoSheetOpen(false)}
            onSelect={handleSelectImage} // 선택한 이미지들 세팅
          />
        </Content>

        { isChatMatch && (
          <ChatInput>
            {/* 선택된 이미지가 있으면 표시 */}
            {selectedImage && (
              <ImgWrapper>
                <img className="userImg"
                  src={selectedImage}
                  alt=""
                />
                <div className="closeBtn">
                  <img src={close} alt="" />
                </div>
              </ImgWrapper>
            )}

            <InputWrapper>
                {/* 현재 재생중인 앨범 커버 */}
                <AlbumCover>
                  <img className="album"
                    src={currentTrack.image} alt="" 
                  />
                  <img className="wave"
                    src={wave}
                    alt="icon"
                  />
                </AlbumCover>
                <AddPhotoButton onClick={() => setAddPhotoSheetOpen(true)}>
                  <img src={plusG600} alt="" />
                </AddPhotoButton>
                <input type="text" 
                  style={{flex: "1", marginLeft: "10px"}}
                  placeholder="오늘은 어떤 노래를 들어볼까요?"
                  value={inputText}
                  onChange={(e) => setInputText(e.target.value)}  
                />
                <SubmitButton onClick={handleChatSubmit}>
                  <img src={upWhite} alt=""/>
                </SubmitButton>
            </InputWrapper>
          </ChatInput>
        )}

        <BottomPlayerArea>

          <audio
            ref={audioRef}
            src={currentTrack.audioUrl}
            autoPlay={isPlaying}
          />
          {isChatMatch && <PlayBar audioRef={audioRef}/>}
        
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
