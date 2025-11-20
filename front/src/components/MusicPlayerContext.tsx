import { createContext, useContext, useState } from "react";

export type TrackType = {
  title: string;
  artist: string;
  url: string;
  image: string;
};

type PlayerContextType = {
  currentTrack: TrackType;
  setCurrentTrack: (t: TrackType) => void;
  isPlaying: boolean;
  setIsPlaying: (v: boolean) => void;
};

const PlayerContext = createContext<PlayerContextType | null>(null);

export function MusicPlayerProvider({ children }: { children: React.ReactNode }) {
  const [currentTrack, setCurrentTrack] = useState<TrackType>({
    title: "Sugar",
    artist: "Maroon 5",
    url: "/audio/sugar-maroon5.mp3",
    // url: "https://open.spotify.com/embed/track/2oAPUnBerGoshpD7ueqwJW",
    image: "/img/sugar_album_cover.jpg",
  });

  const [isPlaying, setIsPlaying] = useState(true);

  return (
    <PlayerContext.Provider
      value={{
        currentTrack,
        setCurrentTrack,
        isPlaying,
        setIsPlaying,
      }}
    >
      {children}
    </PlayerContext.Provider>
  );
}

export const useMusicPlayer = () => {
  const ctx = useContext(PlayerContext);
  if (!ctx) throw new Error("MusicPlayerContext must be used inside Provider");
  return ctx;
};
