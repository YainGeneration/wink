import styled from "styled-components";
import camera from "../../assets/icons/camera.svg";
import { useState } from "react";
import theme from "../../styles/theme";

export default function PhotoGrid({ onSelect }: { onSelect: (img: string) => void }) {
  const [selectedIndexes, setSelectedIndexes] = useState<number[]>([]);

  const photos = [
    "https://picsum.photos/id/1011/200/200",
    "https://picsum.photos/id/1012/200/200",
    "https://picsum.photos/id/1015/200/200",
    "https://picsum.photos/id/1016/200/200",
    "https://picsum.photos/id/1020/200/200",
    "https://picsum.photos/id/1024/200/200",
    "https://picsum.photos/id/1025/200/200",
    "https://picsum.photos/id/1035/200/200",
    "https://picsum.photos/id/1040/200/200",
    "https://picsum.photos/id/1049/200/200",
    "https://picsum.photos/id/1050/200/200"
  ];

  const toggleSelect = (i: number, url: string) => {
    setSelectedIndexes((prev) =>
      prev.includes(i) ? prev.filter((x) => x !== i) : [...prev, i]
    );

    // AddPhoto로 선택된 이미지 전달
    onSelect(url);
  };

  return (
    <Grid>
      <CameraButton>
        <img src={camera} alt="" width={40} height={40} />
      </CameraButton>

      {photos.map((url, i) => {
        const realIndex = i + 1; // 0번이 카메라 버튼이므로 +1

        return (
          <PhotoItem
            key={realIndex}
            selected={selectedIndexes.includes(realIndex)}
            onClick={() => toggleSelect(realIndex, url)}
          >
            <PhotoImg src={url} />
            <SelectCircle selected={selectedIndexes.includes(realIndex)}>
              {selectedIndexes.includes(realIndex)
                ? selectedIndexes.indexOf(realIndex) + 1
                : ""}
            </SelectCircle>
          </PhotoItem>
        );
      })}
    </Grid>
  );
};

const Grid = styled.div`
  display: grid;
  grid-template-columns: repeat(3, 1fr);  /* 한 줄에 3개 */
  gap: 10px;
  width: 100%;
  padding: 16px 0px;
`;

export const CameraButton = styled.div`
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 12px;
  background-color: ${theme.colors.grayscale.g100};
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
`;

export const PhotoItem = styled.div<{ selected: boolean }>`
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 10px;
  overflow: hidden;
  cursor: pointer;
  position: relative;

  border: ${({ selected }) =>
    selected ? "2px solid #FF4DA0" : "1px solid transparent"};

  transition: 0.15s ease;

  img {
    filter: ${({ selected }) => (selected ? "brightness(0.6)" : "none")};
    transition: filter 0.15s ease;
  }
`;

export const PhotoImg = styled.img`
  width: 100%;
  height: 100%;
  object-fit: cover;
`;

export const SelectCircle = styled.div<{ selected: boolean }>`
  position: absolute;
  top: 6px;
  right: 6px;
  width: 26px;
  height: 26px;

  border-radius: 50%;
  background-color: ${({ selected }) => (selected ? "#fff" : "rgba(255,255,255,0.5)")};
  border: ${({ selected }) => (selected ? "1px solid transparent" : "2px solid #fff")};

  font-size: 14px;
  font-weight: bold;
  color: ${({ selected }) => (selected ? "#000" : "transparent")};

  display: flex;
  justify-content: center;
  align-items: center;

  transition: 0.15s ease;
`;
