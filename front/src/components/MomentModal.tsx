import styled from "styled-components";
import theme from "../styles/theme";
import S from "../styles/styled";
import cameraIcon from "../assets/icons/camera.svg";
import closeIcon from "../assets/icons/x.svg";
import worldIcon from "../assets/icons/world.svg";
import playlistIcon from "../assets/icons/playlist.svg";
import rightIcon from "../assets/icons/chevron-right.svg";
import { useRef, useState } from "react";
import ToggleSwitch from "./ToogleSwitch";
import LocationSearchSheet from "./LocationSearchSheet";

export default function MomentModal({
  open,
  onClose,
  image,
  setImage,
  reflectNearbyMusic,
  setReflectNearbyMusic,
  onConfirm,
  selectedLocation,
  setSelectedLocation,
  onOpenLocationSheet,
  setLocationSheetOpen
}: {
  open: boolean;
  onClose: () => void;
  image: string | null;
  setImage: (img: string | null) => void;
  reflectNearbyMusic: boolean;
  setReflectNearbyMusic: (v: boolean) => void;
  onConfirm: () => void;
  selectedLocation: string | null;
  setSelectedLocation: (loc: string | null) => void;
  onOpenLocationSheet: () => void;
  setLocationSheetOpen: (v: boolean) => void; 
}) {

    const fileInputRef = useRef<HTMLInputElement | null>(null);
    

  if (!open) return null;

  const handleImageInput = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = () => setImage(reader.result as string);
    reader.readAsDataURL(file);
  };

  const handleOpenLocation = () => {
    onClose(); // 1) 모달 먼저 닫고
    setTimeout(() => setLocationSheetOpen(true), 10);  // 2) 시트 열기
    };

  return (
    <Overlay onClick={onClose}>
      <Container onClick={(e) => e.stopPropagation()}>

        <HeaderRow>
          <S.Caption>공간의 순간</S.Caption>
          <CloseBtn onClick={onClose}>
            <img src={closeIcon} alt="close" />
          </CloseBtn>
        </HeaderRow>

        <ItemRow>
            <div className="wrapper">
                <img src={worldIcon} alt="" />
                {selectedLocation ? (
                    <S.Body1>{selectedLocation}</S.Body1>
                    ) : (
                    <S.Body1>위치 추가</S.Body1>
                )}
            </div>
            <button
                onClick={() => {
                    if (selectedLocation) {
                        setSelectedLocation(null); // 초기화
                    } else {
                        onOpenLocationSheet(); // 시트 열기
                    }
                }}
            >
                <img src={selectedLocation ? closeIcon : rightIcon} alt="" />
            </button>
        </ItemRow>

        <ItemRow>
            <div className="wrapper">
                <img src={playlistIcon} alt="" />
                <S.Body1>주변 재생 목록 반영</S.Body1>
            </div>
            <ToggleBox>
                <ToggleSwitch
                    checked={reflectNearbyMusic}
                    onChange={(val) => setReflectNearbyMusic(val)}
                />
            </ToggleBox>
        </ItemRow>

        <PhotoArea onClick={() => fileInputRef.current?.click()}>
          {image ? (
            <PreviewImage src={image} alt="preview" />
          ) : (
            <EmptyPhoto>
              <img src={cameraIcon} alt="camera" />
            </EmptyPhoto>
          )}

          <input
            type="file"
            accept="image/*"
            ref={fileInputRef}
            style={{ display: "none" }}
            onChange={handleImageInput}
          />
        </PhotoArea>

        <ActionBtn onClick={onConfirm}>
          <S.Body1 style={{ color: "white"}}>탐색하기</S.Body1>
        </ActionBtn>
      </Container>
        
    </Overlay>
  );
}

/* -------------------- 스타일 -------------------- */

const Overlay = styled.div`
  position: fixed;
  inset: 0;
  background-color: rgba(0,0,0,0.4);
  z-index: 999;
  display: flex;
  justify-content: center;
  align-items: center;
`;

const Container = styled.div`
    position: relative;
  width: 340px;
  background-color: white;
  border-radius: 18px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
`;

const HeaderRow = styled.div`
  display: flex;
  color: ${theme.colors.grayscale.g500};
  justify-content: space-between;
`;

const CloseBtn = styled.button`
  background: none;
  border: none;
`;

const ItemRow = styled.div`
  display: flex;
  justify-content: space-between;
  
  & div.wrapper {
    display: flex;
    align-items: center;
    flex-directions: row;
    gap: 8px;
  }
`;

const ToggleBox = styled.div`
  width: 50px;
`;

const PhotoArea = styled.div`
  width: 100%;
  height: 200px;
  border-radius: 16px;
  background-color: ${theme.colors.grayscale.g100};
  overflow: hidden;
`;

const EmptyPhoto = styled.div`
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  opacity: 0.4;
`;

const PreviewImage = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const ActionBtn = styled.button`
  width: 100%;
  height: 40px;
  background-color: ${theme.colors.primary};
  border-radius: 32px;
  border: none;
`;
