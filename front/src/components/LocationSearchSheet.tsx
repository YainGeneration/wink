import styled from "styled-components";
import S from "../styles/styled";
import searchIcon from "../assets/icons/search-g500.svg";
import { thead } from "framer-motion/client";
import theme from "../styles/theme";
import { createPortal } from "react-dom";
import { distance } from "framer-motion";
import { useState } from "react";

export default function LocationSearchSheet({
  open,
  onClose,
  setSelectedLocation
}: {
  open: boolean;
  onClose: () => void;
  setSelectedLocation: (loc: string) => void;
}) {
  if (!open) return null;

  const [selectedIdx, setSelectedIdx] = useState<number | null>(null);

  const MOCK_LOC = [
    {
        name: "숙명여자대학교 Sookmyung Women's University",
        distance: "0.1km"
    },
    {
        name: "Seoul, South Korea",
        distance: "2.8km"
    },
    {
        name: "서울역 Seoul Station", 
        distance: "1.2km"
    },
    {
        name: "효창공원",
        distance: "0.3km"
    },
    {
        name: "청파동",
        distance: "0.4km"
    },
    {
        name: "용산 아이파크몰",
        distance: "1.8km"
    },
    {
        name: "숙대입구역",
        distance: "0.7km"
    },
  ];

  const handleAdd = () => {
    if (selectedIdx !== null) {
      const selected = MOCK_LOC[selectedIdx].name;
      setSelectedLocation(selected);   // 부모 전달
    }
    onClose();  // 시트 닫기
  };


  return createPortal(
    (<Overlay onClick={onClose}>
      <Sheet onClick={(e) => e.stopPropagation()}>
        <DragHandle />

        <Title><S.Heading3>내 위치 찾기</S.Heading3></Title>

        <SearchBox>
          <img src={searchIcon} alt="search" />
          <input placeholder="검색" />
        </SearchBox>

        <List>
          {MOCK_LOC.map((item, i) => (
            <ListItem
              key={i}
              selected={selectedIdx === i}
              onClick={() => setSelectedIdx(i)}
            >
              <S.Body1>{item.name}</S.Body1>
              <S.Body2 style={{ color: theme.colors.grayscale.g500 }}>
                {item.distance}
              </S.Body2>
            </ListItem>
          ))}
        </List>

        <ConfirmBtn onClick={handleAdd}>
          <S.Heading3 style={{ color: "white"}}>
            위치 추가
          </S.Heading3>
        </ConfirmBtn>
      </Sheet>
    </Overlay>), document.body
  );
}

const Overlay = styled.div`
  position: fixed;
  background-color: rgba(0,0,0,0.4);
  inset: 0;
  display: flex;
  justify-content: center;
  z-index: 1000;
`;

const Sheet = styled.div`
  margin-top: 200px;
  width: 100%;
  max-width: 393px;
  background: white;
  border-radius: 18px 18px 0 0;
  padding: 20px;
  box-sizing: border-box;
  height: fit-content;
  overflow-y: auto;
`;

const DragHandle = styled.div`
  width: 60px;
  height: 6px;
  background: ${theme.colors.grayscale.g400};
  border-radius: 5px;
  margin: 0 auto 16px auto;
`;

const Title = styled.div`
    text-align: center;
  margin-bottom: 14px;
`;

const SearchBox = styled.div`
  height: 44px;
  background: ${theme.colors.grayscale.g50};
  border-radius: 28px;
  display: flex;
  align-items: center;
  padding: 10px 16px;
  gap: 8px;

  & input {
    flex: 1;
    border: none;
    background: transparent;
    outline: none;
  }

  & img {
    width: 24px;
    height: 24px;
  }
`;

const List = styled.div`
  margin: 16px 0;
`;

const ListItem = styled.div<{ selected: boolean }>`
  padding: 12px 8px;
  background-color: ${({ selected }) =>
    selected ? theme.colors.primary_light : "transparent"};
  border-bottom: 1px solid #efefef;
  cursor: pointer;

  &:hover {
    background-color: ${theme.colors.grayscale.g50}
  }
`;

const ConfirmBtn = styled.button`
  width: 100%;
  background: ${theme.colors.primary};
  border-radius: 30px;
  padding: 12px;
  margin-top: 16px;
  border: none;
`;
