import styled from "styled-components";
import theme from "../styles/theme";
import S from "../styles/styled";

interface Props {
  image: string | null; // base64
  extractedSentence: string;
  finalSentence: string;
}

export default function RequestAnalysisContent({
  image,
  extractedSentence,
  finalSentence,
}: Props) {

    const hasImage = image && typeof image === "string";

  return (
    <Card>
        {hasImage && (
        <>
          <ImageContainer>
            <StyledImage src={image} alt="representative" />
          </ImageContainer>

          <SmallLabel><S.Body2>이미지에서 추출했어요</S.Body2></SmallLabel>
          <Sentence><S.Body2>{extractedSentence}</S.Body2></Sentence>
        </>
      )}
        <Divider />
      {/* 사용자 요청 반영 최종 문장 */}
      <SmallLabel><S.Body2>사용자의 멀티모달 요청에서 추출했어요</S.Body2></SmallLabel>
      <Sentence>{finalSentence}</Sentence>
    </Card>
  );
}


const Card = styled.div`
`;



const ImageContainer = styled.div`
  width: 100%;
  border-radius: 10px;
  overflow: hidden;
  height: 200px;
`;

const StyledImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const SmallLabel = styled.div`
  color: ${theme.colors.grayscale.g500};
  margin-top: 12px;
`;

const Sentence = styled.div`
  color: ${theme.colors.grayscale.g800};
  margin-top: 8px;
  line-height: 1.5;
`;

const Divider = styled.div`
  width: 100%;
  border-bottom: 1px solid ${theme.colors.grayscale.g200};
  margin: 8px 0;
`;