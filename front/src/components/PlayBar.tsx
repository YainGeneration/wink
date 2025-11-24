// src/components/PlayBar.tsx
import { useState, useEffect } from "react";
import styled from "styled-components";
import theme from "../styles/theme";
import S from "../styles/styled";
import pauseIcon from "../assets/icons/player-pause.svg";
import playIcon from "../assets/icons/player-play.svg";
import backIcon from "../assets/icons/player-skip-back.svg";
import forwardIcon from "../assets/icons/player-skip-forward.svg";
import playlistIcon from "../assets/icons/playlist.svg";
import { useMusicPlayer } from "./MusicPlayerContext";

const Wrapper = styled.div`
  height: 76px;
  padding: 8px 16px 0px;
  display: flex;
  flex-direction: column;
  background-color: white;
`;

export default function PlayBar({ audioRef }: { audioRef: React.RefObject<HTMLAudioElement | null> }) {
  const { currentTrack, isPlaying, setIsPlaying } = useMusicPlayer();

  const [progress, setProgress] = useState(0);

  useEffect(() => {
    const interval = setInterval(() => {
      if (!audioRef.current || !audioRef.current.duration) return;

      setProgress((audioRef.current.currentTime / audioRef.current.duration) * 100);
    }, 100);

    return () => clearInterval(interval);
  }, [audioRef]);


  return (
    <Wrapper>
      {/* 상단 정보 */}
      <div
        style={{
          display: "flex",
          justifyContent: "space-between",
          width: "100%",
          paddingTop: "10px",
        }}
      >
        {/* 앨범 + 텍스트 */}
        <div style={{ display: "flex", gap: "11px" }}>
          <div
            style={{
              width: "40px",
              height: "40px",
              overflow: "hidden",
              borderRadius: "4px",
            }}
          >
            <img
              src={currentTrack.image}
              style={{ width: "100%", height: "100%", objectFit: "cover" }}
            />
          </div>

          <div style={{ display: "flex", flexDirection: "column", justifyContent: "center" }}>
            <S.Caption>{currentTrack.title}</S.Caption>
            <S.Smalltext style={{ color: theme.colors.grayscale.g600 }}>
              {currentTrack.artist}
            </S.Smalltext>
          </div>
        </div>

        {/* 컨트롤 버튼 */}
        <div style={{ display: "flex", gap: "12px" }}>
          <button><img src={backIcon} /></button>

          <button onClick={() => {
            const newState = !isPlaying;
            setIsPlaying(newState);

            const audio = audioRef.current;
            if (!audio) return;

            if (newState) audio.play();
            else audio.pause();
          }}>
            <img src={isPlaying ? pauseIcon : playIcon} />
          </button>

          <button><img src={forwardIcon} /></button>

          <button><img src={playlistIcon} /></button>
        </div>
      </div>

      {/* 하단 진행 바 */}
      <div
        style={{
          width: "100%",
          height: "2px",
          background: theme.colors.grayscale.g100,
          borderRadius: "3px",
          marginTop: "8px",
          overflow: "hidden",
        }}
      >
        <div
          style={{
            height: "100%",
            width: `${progress}%`,
            background: theme.colors.primary,
            transition: "width 0.1s linear",
          }}
        ></div>
      </div>
    </Wrapper>
  );
}
