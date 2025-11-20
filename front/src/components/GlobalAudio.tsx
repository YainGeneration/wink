import { useEffect, useRef } from "react";
import { useMusicPlayer } from "./MusicPlayerContext";

export default function GlobalAudio() {
  const { currentTrack, isPlaying } = useMusicPlayer();
  const audioRef = useRef<HTMLAudioElement | null>(null);

  // 트랙이 변경되면 새 파일 로드
  useEffect(() => {
    if (!audioRef.current) return;

    audioRef.current.src = currentTrack.url;
    audioRef.current.load();

    if (isPlaying) {
      audioRef.current.play().catch(() => {});
    }
  }, [currentTrack]);

  // 재생/일시정지
  useEffect(() => {
    if (!audioRef.current) return;

    if (isPlaying) audioRef.current.play().catch(() => {});
    else audioRef.current.pause();
  }, [isPlaying]);

  return <audio ref={audioRef} style={{ display: "none" }} />;
}
