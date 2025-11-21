import styled from "styled-components";
import play from "../assets/icons/player-play.svg";
import theme from "../styles/theme";
import S from "../styles/styled";
import MusicItem from "./MusicItem";




type ChatBubbleProps = {
  sender: "user" | "ai";
  text?: string | null;
  image?: string[] | null;
  keywords?: string[];
  recommendations?: any[];
  topic?: string;
  mergedSentence?: string | null;
  imageDescriptionKo?: string | null;
};


export type Recommendation = {
  songId: string;
  title: string;
  artist: string;
  albumCover: string;
  durationFormatted?: string;
};

export default function ChatBubble({
  sender,
  text,
  image,
  keywords = [],
  recommendations = [],
  topic,
  mergedSentence,
  imageDescriptionKo
}: ChatBubbleProps) {
  const isUser = sender === "user";

  return (
    <div>
        <BubbleWrap isUser={isUser}>
        {/* 이미지(선택된 사진 or AI 분석 이미지) */}
        {image && (
            <ImageBox>
            {image && image.length > 0 && (
                <img src={image[0]} />
            )}
            </ImageBox>
        )}

        {/* 메인 텍스트 */}
        {text && <Text isUser={isUser}><S.Body1>{text}</S.Body1></Text>}
        
        {/* 키워드 태그 */}
        {keywords.length > 0 && (
            <TagContainer>
            {keywords.map((k, i) => (
                <Tag key={i}>#{k}</Tag>
            ))}
            </TagContainer>
        )}

        {/* 추천곡 카드 */}
        {recommendations.length > 0 && (
            <div style={{ marginTop: "14px" }}>
                {recommendations.map((r) => (
                <MusicItem
                    key={r.songId}
                    cover={r.albumCover}
                    title={r.title}
                    artist={r.artist}
                    onPlay={() => console.log("play:", r.songId)}
                    onMore={() => console.log("more:", r.songId)}
                />
                ))}
            </div>
            )}
        </BubbleWrap>
    </div>
  );
}

/* ---------------- Styled Components ---------------- */

const BubbleWrap = styled.div<{ isUser: boolean }>`
  display: flex;
  flex-direction: column;
  align-items: ${({ isUser }) => (isUser ? "flex-end" : "flex-start")};
  margin-bottom: 16px;
`;

const Text = styled.div<{ isUser: boolean }>`
  padding: 10px 16px;
  max-width: 78%;
  border-radius: 14px;
  background-color: ${({ isUser }) => (isUser ? theme.colors.primary : theme.colors.grayscale.g50)};
  color: ${({ isUser }) => (isUser ? theme.colors.white : theme.colors.black)};
  white-space: pre-wrap;
`;

const ImageBox = styled.div`
  margin-bottom: 8px;
  img {
    width: 180px;
    height: 180px;
    border-radius: 12px;
    object-fit: cover;
  }
`;

const Topic = styled.div`
  background: #fff7e6;
  padding: 8px 10px;
  font-size: 13px;
  border-radius: 8px;
  margin-top: 4px;
  width: 92%;
`;

const Desc = styled.div`
  margin-top: 8px;
  padding: 8px 12px;
  font-size: 13px;
  background: #fafafa;
  border-radius: 8px;
  width: 92%;
  line-height: 1.45;
`;

const TagContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 10px;
  max-width: 92%;
`;

const Tag = styled.div`
  padding: 6px 10px;
  background: ${theme.colors.primary_light};
  font-size: 12px;
  border-radius: 12px;
  color: ${theme.colors.grayscale.g800};
`;