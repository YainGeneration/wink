import styled from "styled-components";
import { useMusicPlayer } from "../components/MusicPlayerContext";
import theme from "../styles/theme";
import S from "../styles/styled";

import search from "../assets/icons/search-white.svg"
import mix1 from "../../public/img/mix1.png"
import mix2 from "../../public/img/mix2.jpg"
import play from "../assets/icons/player-play.svg"
import playlist1 from "../../public/img/playlist1.jpg"
import playlist2 from "../../public/img/playlist2.jpg"
import playlist3 from "../../public/img/playlist3.jpg"
import music1 from "../../public/img/recommend1.jpg"
import music2 from "../../public/img/recommend2.jpeg"
// import music3 from "../../public/img/recommend3.jpg"
import MusicItemForDark from "../components/MusicItemForDark";

export default function Recommend() {

  const PrimaryBackground = styled.div`
    background-color: ${theme.colors.primary};
    width: 100%;
    height: 288px;
    position: relative;
    border-radius: 0px 0px 0px 40px;

    & .sub {
      position: absolute;
      left: -16px;
      top: 150px;
      z-index: 11;
      color: ${theme.colors.white};
      transform: rotate(-90deg);
    } 
  `

  const Wrapper = styled.div`
    display: flex;
    height: 58px;
    margin-top: 50px;
    width: 100%;
    padding: 16px;
    justify-content: space-between;
    align-items: center;
    background-color: transparent;
    position: absolute;
    color: ${theme.colors.white}
  `;

  const MixCard = styled.div`
    width: 220px;
    height: 280px;
    border-radius: 20px;
    position: relative;
    left: 20%;
    top: 110px;
    z-index: 2;
    overflow: hidden;

    & img.cover {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    & .play-btn {
      position: absolute;
      z-index: 11;
      top: 12px;
      right: 12px;
      background-color: ${theme.colors.white};
      padding: 12px;
      border-radius: 40px;
      width: 48px;
      height: 48px;
    }

    & .mix-title {
      position: absolute;
      z-index: 11;
      bottom: 16px;
      left: 18px;
      color: ${theme.colors.white}
    }
  `

  const MixCard2 = styled.div`
    width: 220px;
    height: 280px;
    border-radius: 20px;
    position: relative;
    left: 82%;
    top: -170px;
    z-index: 10;
    overflow: hidden;

    & img.cover {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }

    & .play-btn {
      position: absolute;
      z-index: 11;
      top: 12px;
      right: 12px;
      background-color: ${theme.colors.white};
      padding: 12px;
      border-radius: 40px;
      width: 48px;
      height: 48px;
    }

    & .mix-title {
      position: absolute;
      z-index: 11;
      bottom: 16px;
      left: 18px;
      color: ${theme.colors.white}
    }
  `

  const WhiteTextColor = styled.div`
    color: ${theme.colors.white};
    margin-left: 24px;
    margin-bottom: 10px;
  `

  const PlayListWrapper = styled.div`
    margin-left: 24px;
    width: 100%;
    display: flex;
    flex-direction: row;
    gap: 12px;
  `

  const PlayList = styled.div`
    width: 124px;
    height: 158px;
    background-color: ${theme.colors.black_light};
    border-radius: 12px;

    & .imgWrapper {
      overflow: hidden;
      width: 100%;
      height: 96px;
      border-radius: 12px;
    }

    & img {
      width: 100%;
      height: 100%;
      object-fit: cover;
    }
  `

  return (
    <>
      <PrimaryBackground>
        <Wrapper>
          <S.Heading2>안녕하세요, 예인님</S.Heading2>
          <img src={search} alt="" />
        </Wrapper>
        <MixCard>
          <img className="mix" src={mix1} alt="" />
          <div className="play-btn">
            <img src={play} alt="" />
          </div>
          <div className="mix-title"><S.Heading1>Pop Mix</S.Heading1></div>
        </MixCard>
        <MixCard2>
          <img className="mix" src={mix2} alt="" />
          <div className="play-btn">
            <img src={play} alt="" />
          </div>
          <div className="mix-title"><S.Heading1>KPop Mix</S.Heading1></div>
        </MixCard2>
        <div className="sub">
          <S.Heading3>Mix For You</S.Heading3>
        </div>
      </PrimaryBackground>
      <div style={{height: "120px"}}></div>
      <WhiteTextColor>
        <S.Heading3>들었던 곡과 비슷한 음악</S.Heading3>
      </WhiteTextColor>
      <PlayListWrapper>
        <PlayList>
          <div className="imgWrapper">
            <img src={playlist2} alt="" />
          </div>
          <div
            style={{
              width: "106px",
              margin: "6px 8px",
            }}
          >
            <div style={{
              display: "flex",
              flexDirection: "column",
              gap: "4px"
            }}>
              <div
                style={{color: theme.colors.white}}
              >
                <S.Caption>2025 마지막을 장식해 줄 엔딩 크레딧</S.Caption>
              </div>
              <div
                style={{color: theme.colors.grayscale.g200}}
              >
                <S.Caption>foreven.</S.Caption>
              </div>
            </div>
          </div>
        </PlayList>
        <PlayList>
          <div className="imgWrapper">
            <img src={playlist1} alt="" />
          </div>
          <div
            style={{
              width: "106px",
              margin: "6px 8px",
            }}
          >
            <div style={{
              display: "flex",
              flexDirection: "column",
              gap: "4px"
            }}>
              <div
                style={{color: theme.colors.white}}
              >
                <S.Caption>느긋한 여유를 더하는 카페 재질 인디 음악</S.Caption>
              </div>
              <div
                style={{color: theme.colors.grayscale.g200}}
              >
                <S.Caption>INDIE마스터</S.Caption>
              </div>
            </div>
          </div>
        </PlayList>
        <PlayList>
          <div className="imgWrapper">
            <img src={playlist3} alt="" />
          </div>
          <div
            style={{
              width: "106px",
              margin: "6px 8px",
            }}
          >
            <div style={{
              display: "flex",
              flexDirection: "column",
              gap: "4px"
            }}>
              <div
                style={{color: theme.colors.white}}
              >
                <S.Caption>새벽은 바다가 되고, 우울은 배가 되어</S.Caption>
              </div>
              <div
                style={{color: theme.colors.grayscale.g200}}
              >
                <S.Caption>듣는순간</S.Caption>
              </div>
            </div>
          </div>
        </PlayList>
      </PlayListWrapper>
      <div style={{height: "20px"}}></div>
      <WhiteTextColor>
        <S.Heading3>매일 새로운 맞춤 선곡</S.Heading3>
      </WhiteTextColor>
      <div
        style={{
          margin: "0px 24px"
        }}
      >
        <MusicItemForDark
          cover={music1}
          title="ICU (쉬어가도 돼)"
          artist="aespa"
        />
        <MusicItemForDark
          cover={music2}
          title="1999"
          artist="마크 (MARK)"
        />
        <MusicItemForDark
          cover={mix2}
          title="紅蓮の弓矢 (홍련의 화살)"
          artist="후마레타 하나노"
        />
      </div>
      

    </>
  );
}
