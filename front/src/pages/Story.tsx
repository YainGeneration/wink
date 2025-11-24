import React from 'react';
import S from '../styles/styled';
import styled from 'styled-components';
import { style } from 'framer-motion/client';
import theme from '../styles/theme';
import user1 from "../../public/img/user1.webp"
import user2 from "../../public/img/user2.webp"
import user3 from "../../public/img/user3.webp"
import music1 from "../../public/img/music1.jpg"
import music2 from "../../public/img/music2.jpg"
import music3 from "../../public/img/music3.jpeg"
import MusicItem from '../components/MusicItem';
import heartFilled from "../assets/icons/heart-fill-primary.svg"
import musicShare from "../assets/icons/music-share.svg"
import heartEmpty from "../assets/icons/heart-g500.svg"

const Wrapper = styled.div`
  display: flex;
  height: 58px;
  margin-top: 44px;
  padding: 16px 0px;
  background-color: transparent;
  justify-content: center;
`;

const Card = styled.div`
    padding: 18px;
    background-color: ${theme.colors.white};
    margin: 0px 16px;
    border-radius: 30px;
    display: flex;
    flex-direction: column;
`;

const ProfileWrapper = styled.div`
    display: flex;
    margin-bottom: 12px;

    & .img-wrapper {
        width: 56px;
        height: 56px;
        overflow: hidden;
        border-radius: 30px;
    }

    & .profile{
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    & .info-wrapper {
        justify-content: center;
        display: flex;
        flex-direction: column;
        margin-left: 10px;
        gap: 4px;

        > :nth-child(2) {
            color: ${theme.colors.grayscale.g500};
        }
    }
`;

const BottomInfo = styled.div`
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    margin-top: 20px;

    & .heart {
        display: flex;
        flex-direction: row;
        align-items: center;
        gap: 4px;
    }
`



const Story = () => {
    return (
        <>
            <Wrapper>
                <S.Heading3>유저들의 스토리</S.Heading3>
            </Wrapper>
            <div style={{display: "flex", flexDirection: "column", gap: "14px"}}>
                <Card>
                    <ProfileWrapper>
                        <div className='img-wrapper'>
                            <img className="profile" src={user1} alt="" />
                        </div>
                        <div className='info-wrapper'style={{}}>
                            <S.Caption>데일리앤써</S.Caption>
                            <S.Caption>1분 전</S.Caption>
                        </div>
                    </ProfileWrapper>
                    <div style={{marginBottom: "20px"}}>
                        <S.Body1>
                            너무 좋아요 이번 신곡 그냥 미쳣슨
                        </S.Body1>
                        <S.Body1>
                            JYP 드디어 일한다 4본부 최고다 아자쓰!
                        </S.Body1>
                    </div>
                    <MusicItem
                        cover={music1}
                        title="Blue Valentine"
                        artist="NMIXX"
                    />
                    <BottomInfo>
                        <div className='heart'>
                            <img src={heartFilled} alt="" />
                            <S.Body2>52</S.Body2>
                        </div>
                        <img src={musicShare} alt="" />
                    </BottomInfo>
                </Card>
                <Card>
                    <ProfileWrapper>
                        <div className='img-wrapper'>
                            <img className="profile" src={user2} alt="" />
                        </div>
                        <div className='info-wrapper'style={{}}>
                            <S.Caption>리바이추종자</S.Caption>
                            <S.Caption>4분 전</S.Caption>
                        </div>
                    </ProfileWrapper>
                    <div style={{marginBottom: "20px"}}>
                        <S.Body1>
                            진격의 거인은 한 편의 역사와 같다.
                        </S.Body1>
                        <S.Body1>
                            수 백번 들었지만, 언제쯤 안 울 수 있을까.
                        </S.Body1>
                    </div>
                    <MusicItem
                        cover={music2}
                        title="紅蓮の弓矢 (홍련의 화살)"
                        artist="후마레타 하나노"
                    />
                    <BottomInfo>
                        <div className='heart'>
                            <img src={heartEmpty} alt="" />
                            <S.Body2>196</S.Body2>
                        </div>
                        <img src={musicShare} alt="" />
                    </BottomInfo>
                </Card>
                <Card>
                    <ProfileWrapper>
                        <div className='img-wrapper'>
                            <img className="profile" src={user3} alt="" />
                        </div>
                        <div className='info-wrapper'style={{}}>
                            <S.Caption>진수</S.Caption>
                            <S.Caption>10분 전</S.Caption>
                        </div>
                    </ProfileWrapper>
                    <div style={{marginBottom: "20px"}}>
                        <S.Body1>
                            아이콘 너네 언제 돌아와?
                        </S.Body1>
                        <S.Body1>
                            나 아직 이 곡에서 멈춰 있는데 ...
                        </S.Body1>
                    </div>
                    <MusicItem
                        cover={music3}
                        title="줄게(JUST FOR YOU)"
                        artist="iKON"
                    />
                    <BottomInfo>
                        <div className='heart'>
                            <img src={heartFilled} alt="" />
                            <S.Body2>98</S.Body2>
                        </div>
                        <img src={musicShare} alt="" />
                    </BottomInfo>
                </Card>
            </div>
        </>
    );
};

export default Story;