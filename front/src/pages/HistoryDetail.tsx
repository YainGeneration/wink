import styled from "styled-components";
import S from "../styles/styled";
import theme from "../styles/theme";

const Card = styled.div`
  background-color: ${theme.colors.white};
  padding: 12px 16px 14px;
  margin-top: 16px;
  border-radius: 10px;
  box-shadow: ${theme.shadow.default};
`


const HistoryDetail = () => {
    return (
        <S.Padding16px>
            <div
                style={{
                    marginLeft: "14px"
                }}
            >
                <S.Heading2>독서 중 재즈 추천</S.Heading2>
                <div
                    style={{
                        marginTop: "8px",
                        color: theme.colors.grayscale.g500
                    }}
                >
                    <S.Body1>2025.10.28 - 2025.10.31</S.Body1>
                </div>
            </div>
            <div>
          <Card>
            <div
                style={{display: "flex", flexDirection: "column", gap: "8px", color: theme.colors.grayscale.g600}}
            >
                <S.Heading3>사용자의 입력 히스토리</S.Heading3>
                <S.Body1>입력 히스토리 요약</S.Body1>
            </div>
          </Card>
        </div>
        </S.Padding16px>
    );
};

export default HistoryDetail;