import styled from "styled-components";
import S from "../../styles/styled";
import theme from "../../styles/theme";
import search from "../../assets/icons/search.svg"
import pencil from "../../assets/icons/pencil.svg"
import camera from "../../assets/icons/camera.svg"
import PhotoGrid from "./PhotoGrid";
import { useState } from "react";


const Sheet = styled.div<{ open: boolean }>`
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  height: 410px;
  background: white;
  border-radius: 20px 20px 0 0;
  transform: translateY(${({ open }) => (open ? "0%" : "100%")});
  transition: transform 0.25s ease-in-out;
  z-index: 50;
  padding: 8px 16px 0px;
  display: flex;
  flex-direction: column;
  align-items: center;
  overflow: hidden; /* 내부 스크롤 허용을 위해 */

`;

const SlideBar = styled.div`
    display: block;
    width: 60px;
    height: 6px;
    background-color: ${theme.colors.grayscale.g400};
    border-radius: 4px;
`;

const Dimmed = styled.div<{ open: boolean }>`
  position: absolute;
  inset: 0;
  background: rgba(18,18,18,0.1);
  z-index: 40;
  opacity: ${({ open }) => (open ? 1 : 0)};
  pointer-events: ${({ open }) => (open ? "auto" : "none")};
  transition: opacity 0.25s ease-in-out;
`;

// const PhotoGrid = styled.div`
//   display: grid;
//   grid-template-columns: repeat(3, 1fr);  /* 한 줄에 3개 */
//   gap: 12px;
//   width: 100%;
//   padding: 16px;
// `;

const PhotoItem = styled.div`
  width: 100%;
  aspect-ratio: 1 / 1;  /* 정사각형 유지 */
  border-radius: 12px;
  overflow: hidden;
  position: relative;
`;

const PhotoImg = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

const CameraButton = styled.button`
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  background-color: ${theme.colors.grayscale.g100};
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
`;

const ScrollArea = styled.div`
  flex: 1; /* 남은 공간 모두 차지 */
  overflow-y: auto;
  margin-top: 16px;
  padding-right: 4px; /* 스크롤바 여백 */
  width: 100%;
`;

const AddButton = styled.button`
  height: 40px;
  width: 94%;
  margin-bottom: 20px;
  border-radius: 24px;
  border: none;
  background-color: #ff4da0;
  color: white;
  position: absolute;
  bottom: 0;
`;


export default function AddPhoto({ open, onClose, onSelect }: { open: boolean; onClose: () => void; onSelect: (img: string) => void; }) {

    const [selectedImages, setSelectedImages] = useState<string[]>([]);

    return (
    <>
      <Dimmed open={open} onClick={onClose} />
      <Sheet open={open}>
        <SlideBar />

        <ScrollArea>
            <PhotoGrid
                onSelect={(img) => {
                    setSelectedImages([img]);
                    onSelect(img);  
                }}
            />
        </ScrollArea>

        <AddButton
          onClick={() => {
            if (selectedImages.length > 0) {
              onSelect(selectedImages[0]); // 첫 번째 선택된 이미지 전달
            }
            onClose();
          }}
        >
            <S.Heading3>추가하기</S.Heading3>
        </AddButton>
      </Sheet>

    </>
  );
}
