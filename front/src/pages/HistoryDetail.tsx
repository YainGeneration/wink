import styled from "styled-components";
import S from "../styles/styled";
import theme from "../styles/theme";
import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import HistoryAccordion from "../components/HistoryAccordion";
import MusicItem from "../components/MusicItem";
import { formatDate } from "../utils/formatDate";
import RequestAnalysisContent from "../components/RequestAnalysisContent";

const Card = styled.div`
  background-color: ${theme.colors.white};
  padding: 12px 16px 14px;
  margin-top: 16px;
  border-radius: 10px;
  box-shadow: ${theme.shadow.default};
`

const MusicListWrapper = styled.div`
  display: flex;
  flex-direction: column;
`;

const TagContainer = styled.div`
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
`;

const Tag = styled.div`
  height: 24px;
  justify-contents: center;
  padding: 4px 12px;
  border-radius: 20px;
  background-color: ${theme.colors.primary_light};
  color: ${theme.colors.primary};
  border: 1px solid ${theme.colors.primary}
`;



function getSubtitle(recommendations: any[]) {
  if (!recommendations || recommendations.length === 0) return "추천 결과 없음";

  const firstTitle = recommendations[0].title;
  const count = recommendations.length - 1;

  return count > 0
    ? `${firstTitle} 외 ${count}곡`
    : firstTitle; // 곡이 1개뿐이면 '첫 곡만'
}

const SummaryTags = ({ keywords }: { keywords: string[] }) => (
  <TagContainer>
    {keywords.map((k: string) => (
      <Tag key={k}><S.Caption>#{k}</S.Caption></Tag>
    ))}
  </TagContainer>
);


const HistoryDetail = () => {
    const { sessionId } = useParams(); // URL의 40 읽기
    const [data, setData] = useState<any>(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
    if (!sessionId) return;

    const fetchHistoryDetail = async () => {
        try {
            const response = await fetch(`http://localhost:8080/api/chat/${sessionId}/summary`);
            const result = await response.json();

            console.log(result)

            setData(result);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
        };

        // 연결할 때는 여기 주석 처리
        // setData(MOCK_DATA);
        setLoading(false);

        fetchHistoryDetail();
    }, [sessionId]);

    if (loading) return <div></div>;
    if (!data) return <div></div>;

    return (
        <S.Padding16px>
            <div
                style={{
                    marginLeft: "14px"
                }}
            >
              {/* <S.Heading2>{data.topic}</S.Heading2> */}
              <S.Heading2>잔상, 벅찬 밤의 여운</S.Heading2>
                <div
                    style={{
                        marginTop: "8px",
                        color: theme.colors.grayscale.g500
                    }}
                >
                    <S.Body1>
                        {formatDate(data.timestamp)} 
                        {data.endTime ? ` - ${formatDate(data.endTime)}` : ""}
                    </S.Body1>
                </div>
            </div>
            <div>
          <Card>
            <div
                style={{display: "flex", flexDirection: "column", gap: "8px", color: theme.colors.grayscale.g600}}
            >
                <S.Heading3>사용자의 입력 히스토리</S.Heading3>
                <S.Body2>{data.summaryMode.summary}</S.Body2>
            </div>
          </Card>
          <HistoryAccordion
            title="사용자의 요청 분석 결과"
            subtitle={<SummaryTags keywords={data.summaryMode.keywords} />} 
        >
            <RequestAnalysisContent
                image={data.representativeImages[0]}
                extractedSentence={data.summaryMode.english_caption_from_agent2}
                finalSentence={data.summaryMode.interpretedSentence}
            />
        </HistoryAccordion>
          <HistoryAccordion
            title="음악 추천 결과"
            subtitle={getSubtitle(data.summaryMode.recommendations)}
            >
                <MusicListWrapper>
                    {data.summaryMode.recommendations.map((song:any) => (
                    <MusicItem
                        key={song.songId}
                        cover={song.albumCover}
                        title={song.title}
                        artist={song.artist}
                        onPlay={() => console.log("play", song.songId)}
                        onMore={() => console.log("more", song.songId)}
                    />
                    ))}
                </MusicListWrapper>
          </HistoryAccordion>
        </div>
        </S.Padding16px>
    );
};

export default HistoryDetail;