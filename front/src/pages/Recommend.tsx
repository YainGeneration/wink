import { useMusicPlayer } from "../components/MusicPlayerContext";

export default function Recommend() {
  // const { setTrack } = useMusicPlayer();

  // const handlePlay = (track) => {
  //   setTrack({
  //     title: track.title,
  //     artist: track.artist,
  //     image: track.albumImage,
  //     url: track.previewUrl,   // 또는 서버 mp3
  //   });
  // };

  return (
    <div>
      안녕
      {/* {tracks.map((t) => (
        <button onClick={() => handlePlay(t)}>
          <img src={t.albumImage} />
          {t.title}
        </button>
      ))} */}
    </div>
  );
}
