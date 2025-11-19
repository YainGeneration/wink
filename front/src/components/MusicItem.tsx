import styled from "styled-components";
import dots from "../assets/icons/dots-vertical-g600.svg";
import play from "../assets/icons/player-play-g600.svg";

interface MusicItemProps {
  cover: string;        // 앨범커버 이미지 URL
  title: string;        // 노래 제목
  artist: string;       // 가수 이름
  onPlay?: () => void;  // 재생 버튼 클릭 시
  onMore?: () => void;  // 더보기 버튼 클릭 시
}

export default function MusicItem({
  cover,
  title,
  artist,
  onPlay,
  onMore,
}: MusicItemProps) {
  return (
    <ItemWrapper>
      <Cover src={cover} alt={title} />

      <InfoWrapper>
        <Title>{title}</Title>
        <Artist>{artist}</Artist>
      </InfoWrapper>

      <RightButtons>
        <IconButton onClick={onPlay}>
          <img src={play} alt="play" />
        </IconButton>
        <IconButton onClick={onMore}>
          <img src={dots} alt="more" />
        </IconButton>
      </RightButtons>
    </ItemWrapper>
  );
}

/* ---------------- Styled Components ---------------- */

const ItemWrapper = styled.div`
  width: 100%;
  display: flex;
  align-items: center;
  padding: 8px 0;
  gap: 12px;
`;

const Cover = styled.img`
  width: 56px;
  height: 56px;
  border-radius: 8px;
  object-fit: cover;
`;

const InfoWrapper = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
`;

const Title = styled.div`
  font-size: 16px;
  font-weight: 600;
  color: #1c1c1e;
`;

const Artist = styled.div`
  font-size: 14px;
  color: #8e8e93;
`;

const RightButtons = styled.div`
  display: flex;
  align-items: center;
  gap: 8px;
`;

const IconButton = styled.button`
  background: none;
  border: none;
  padding: 0;
`;
