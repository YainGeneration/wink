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



const MOCK_DATA = {
  sessionId: 55,
  type: "MY",
  topic: "노을빛 아래, 집중을 부르는 선율",
  representativeText: "조금 더 밝은 노래도 추천해줘",
//   representativeImages: null,
  representativeImages: "https://picsum.photos/200/200?random=5",
  latestUserSummary: "밝은 노래 추천 부탁\n",
  summaryMode: {
    summary:
      "사용자는 해 질 때 집중하기 좋은 노래를 추천받고 싶어 했습니다. 시스템은 여러 번 음악을 추천했으나, 사용자는 더 밝은 느낌의 곡을 요청했습니다.",
    keywords: ["도시 풍경", "잔잔한", "따스한", "생기 넘치는", "평온한"],
    recommendations: [
      {
        songId: "1",
        title: "Blue Valentine",
        artist: "NMIXX",
        albumCover: "https://picsum.photos/200/200?random=1",
      },
      {
        songId: "2",
        title: "French Inhale",
        artist: "bsd.u",
        albumCover: "https://picsum.photos/200/200?random=2",
      },
      {
        songId: "3",
        title: "I Love Kanye",
        artist: "Kanye West",
        albumCover: "https://picsum.photos/200/200?random=3",
      },
      {
        songId: "4",
        title: "MEGALOVANIA",
        artist: "Toby Fox",
        albumCover: "https://picsum.photos/200/200?random=4",
      },
    ],
    "english_caption_from_agent2": "안녕하세요~ 이건 이미지에서 뽑아낸거에요",
    "mergedSentence": "I recommend a brighter song if possible, while the image captures a serene cityscape at sunset, where the warm orange hues of the sky are reflected off the buildings, creating a tranquil yet vibrant scene.",
    "interpretedSentence": "석양이 도시를 부드럽게 감싸 안고, 건물들에 따스한 주황빛이 물드는 평화로운 풍경이 눈앞에 펼쳐지니, 차분하면서도 생동감 넘치는 이 장면에 좀 더 밝고 경쾌한 멜로디를 입혀보면 어떨까 싶어요.\n"
  },
  timestamp: "2025-11-20T13:13:32.686525",
  endTime: "2025-11-20T13:25:12.686525",
  latest: false,
};


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
            const response = await fetch(`http://localhost:8080/history/${sessionId}`);
            const result = await response.json();

            console.log(response)

            setData(result);
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
        }
        };

        // 연결할 때는 여기 주석 처리
        setData(MOCK_DATA);
        setLoading(false);

        fetchHistoryDetail();
    }, [sessionId]);

    if (loading) return <div>로딩중...</div>;
    if (!data) return <div>데이터가 없습니다</div>;

    return (
        <S.Padding16px>
            <div
                style={{
                    marginLeft: "14px"
                }}
            >
                <S.Heading2>{data.topic}</S.Heading2>
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
                image={data.representativeImages}
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