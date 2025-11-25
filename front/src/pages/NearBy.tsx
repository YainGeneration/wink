import React, { useEffect, useState } from 'react';
import { useNavigate } from "react-router-dom";
import map from '../../public/img/map.png'
import styled from 'styled-components';
import { style } from 'framer-motion/client';
import theme from '../styles/theme';
import S from '../styles/styled';
import camera from "../assets/icons/camera-white.svg"
import MusicItem from '../components/MusicItem';
import MomentModal from '../components/MomentModal';
import LocationSearchSheet from '../components/LocationSearchSheet';

interface NearbyUser {
  userId: number;
  nickname: string;
  songTitle: string;
  artist: string;
  albumCover: string;
  lat: number;
  lng: number;
  top?: string;
  left?: string;
}

export default function NearBy () {

  const [loading, setLoading] = useState(true);
  const [users, setUsers] = useState<NearbyUser[]>([]);
  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [selectedUser, setSelectedUser] = useState<any>(null);

  const [reflectNearbyMusic, setReflectNearbyMusic] = useState(false);

  const [momentModalOpen, setMomentModalOpen] = useState(false);
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [selectedLocation, setSelectedLocation] = useState<string | null>(null);
  const [locationSheetOpen, setLocationSheetOpen] = useState(false);


  
  const navigate = useNavigate();

  // 🔥 위치값 (임시 mock)
  const currentLat = 37.546312;
  const currentLng = 126.967111;

  const FIXED_POSITIONS = [
  { top: "160px", left: "56%" },
  { top: "230px", left: "80%" },
  { top: "400px", left: "10%" },
  { top: "300px", left: "92%" },
  { top: "700px", left: "50%" },
];
  // ---- 가라데이터(Mock) ----

  const MOCK_USERS = [
    {
      userId: 1,
      nickname: "사용자1",
      songTitle: "Yoga",
      artist: "Spa Background Music",
      albumCover: "../../public/img/near1.jpeg",
      lat: 1.000494349293526,
      lng: 0.9994459724129843,
    },
    {
      userId: 2,
      nickname: "사용자2",
      songTitle: "To you my Love",
      artist: "DANIEL H",
      albumCover: "../../public/img/near2.jpeg",
      lat: 1.0006338883348183,
      lng: 1.0004808060606825,
    },
    {
      userId: 3,
      nickname: "사용자3",
      songTitle: "Jocelyn - Cyril Ury - MdC",
      artist: "Cyril Ury",
      albumCover: "../../public/img/near3.jpeg",
      lat: 1.0008768170182383,
      lng: 1.0008959796774475,
    },
    {
      userId: 4,
      nickname: "사용자4",
      songTitle: "The Last Stand",
      artist: "Grégoire Lourme",
      albumCover: "../../public/img/near4.jpeg",
      lat: 0.9991623679534147,
      lng: 0.9998732092750647,
    },
    {
      userId: 5,
      nickname: "사용자5",
      songTitle: "Movement IX - Fibonacci Theorem (Orchestral Version)",
      artist: "JCRZ",
      albumCover: "../../public/img/near5.jpeg",
      lat: 1.0004559626344167,
      lng: 1.0001181526402254,
    },
  ];

  useEffect(() => {
    let timer = setTimeout(async () => {
      // --------------------
      // 🔥 백엔드 연결 전: 가라데이터 사용
      // --------------------
      setUsers(MOCK_USERS);
      setLoading(false);

      // 🔥 백엔드 연결 후 다시 활성화
      /*
      try {
        const res = await fetch("http://localhost:8080/api/location/nearby-music");
        const data = await res.json();
        setUsers(data);
      } catch (err) {
        console.error("nearby fetch error:", err);
      } finally {
        setLoading(false);
      }
      */
    }, 200);

    return () => clearTimeout(timer);
  }, []);

  const handleUserClick = (user: NearbyUser) => {
    setSelectedUserId(user.userId);
    setSelectedUser(user);
  };

  const resetSelection = () => {
    setSelectedUserId(null);
    setSelectedUser(null);
  };

  if (loading) return <div>로딩 중...</div>;

  const handleExplore = async () => {
    try {
      const payload = {
        location: {
          lat: currentLat,
          lng: currentLng,
        },
        reflectNearbyMusic: reflectNearbyMusic,  // 토글값
        image: selectedImage,                    // base64 이미지
      };

      await fetch("http://localhost:8080/api/location/explore", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(payload),
      });

      // 성공 후 채팅 화면으로 이동
      navigate("/chat");
      setMomentModalOpen(false);
    } catch (err) {
      console.error("탐색하기 요청 실패:", err);
    }
};

if (loading) return <div>로딩 중...</div>;

  return (
    <>
      <Wrapper>
        <Header>
          <FindMusicBtn onClick={() => setMomentModalOpen(true)}>
            <S.Body2>내 공간에 맞는 음악 찾기</S.Body2>
            <img src={camera} alt="" />
          </FindMusicBtn>
        </Header>
        <NearByContent onClick={resetSelection}>
          <MyLoc top="57%" left="47%"></MyLoc>
          {users.map((user, index) => {
            const pos = FIXED_POSITIONS[index];

            return (
              <InstanceBtn
                key={user.userId}
                top={pos.top}
                left={pos.left}
                selected={selectedUserId === user.userId}
                onClick={(e) => {
                  e.stopPropagation();
                  handleUserClick(user);
                }}
              />
            );
          })}

          {/* 선택된 유저의 음악 카드 */}
          {selectedUser && (
            <SelectedMusicCard onClick={(e) => e.stopPropagation()}>
              <S.Body2>청파동 부근에서</S.Body2>

              <MusicItem
                cover={selectedUser.albumCover}
                title={selectedUser.songTitle}
                artist={selectedUser.artist}
              />
            </SelectedMusicCard>
          )}

        </NearByContent>
        <MapImgWrapper>
          <MapImg src={map} alt="" />
        </MapImgWrapper>

        
      </Wrapper>
      <MomentModal
            open={momentModalOpen}
            onClose={() => setMomentModalOpen(false)}
            image={selectedImage}
            setImage={setSelectedImage}
            reflectNearbyMusic={reflectNearbyMusic}
            setReflectNearbyMusic={setReflectNearbyMusic}
            selectedLocation={selectedLocation}
            setSelectedLocation={setSelectedLocation}
            setLocationSheetOpen={setLocationSheetOpen}
            onOpenLocationSheet={() => {
              setMomentModalOpen(false);
              setLocationSheetOpen(true);
            }}
            onConfirm={handleExplore}
        />
      <LocationSearchSheet 
        open={locationSheetOpen}
        onClose={() => setLocationSheetOpen(false)}
        setSelectedLocation={(loc: string) => {
          setSelectedLocation(loc);
          setLocationSheetOpen(false);
          setMomentModalOpen(true);  // 모달 다시 열기
        }}
      />
    </>
  );
};

const Wrapper = styled.div`
  width: 100%;
  height: 100%;
  position: relative;
`

const Header = styled.div`
  width: 100%;
  height: 50px;
  position: absolute;
  background-color: transparent;
  top: 0;
  z-index: 10;
  margin-top: 58px;

  & button:hover {
    filter: brightness(0.95);
  }
`

const FindMusicBtn = styled.button`
  background-color: ${theme.colors.primary};
  color: ${theme.colors.white};
  padding: 6px 12px;
  border-radius: 24px;
  display: flex;
  flex-direction: row;
  gap: 4px;
  margin: 0 0px 0px 200px;
  box-shadow: ${theme.shadow.default};
`;


const NearByContent = styled.div`
  width: 100%;
  height: 100%;
  z-index: 1;
  position: absolute;
  background-color: transparent;
`

const InstanceBtn = styled.button<{ top: string, left: string, selected?: boolean }>`
  width: 14px;
  height: 14px;
  display: block;
  border-radius: 50px;
  background-color: ${({ selected }) =>
    selected ? theme.colors.primary : theme.colors.grayscale.g600};
  box-shadow: ${theme.shadow.pin};
  position: absolute;
  top: ${({ top }) => top};
  left: ${({ left }) => left};
  transition: background-color 0.2s ease;
`

const MyLoc = styled.div<{ top: string, left: string }>`
  width: 20px;
  height: 20px;
  display: block;
  border-radius: 50px;
  background-color: ${theme.colors.primary};
  border: 2px solid ${theme.colors.white};
  box-shadow: 0px 0px 30px 0px rgba(250, 78, 171, 1);
  position: absolute;  
  top: ${({ top }) => top};
  left: ${({ left }) => left};
`



const MapImgWrapper = styled.div`
  width: 100%;
  height: 100%;
  overflow: hidden;
  position: absolute;
`

const MapImg = styled.img`
  object-fit: cover;
  width: 100%;
  height: 100%;
`

const SelectedMusicCard = styled.div`
  position: absolute;
  bottom: 18px;
  left: 16px;
  right: 16px;
  padding: 14px 18px;
  background-color: white;
  border-radius: 20px;
  box-shadow: ${theme.shadow.default};
  z-index: 20;

  display: flex;
  flex-direction: column;
`;