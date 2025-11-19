import styled from "styled-components";
import S from "../styles/styled";
import theme from "../styles/theme";
import search from "../assets/icons/search.svg"
import pencil from "../assets/icons/pencil.svg"
import AccordionMenu from "./AccordionMenu";

const SidebarWrap = styled.div<{ open: boolean }>`
  position: absolute;
  top: 0;
  left: 0;
  width: 260px;
  height: 100%;
  background: white;
  z-index: 50;
  padding-top: 10px;

  transform: ${({ open }) => (open ? "translateX(0)" : "translateX(-100%)")};
  transition: transform 0.4s ease;
`;

const Dimmed = styled.div<{ open: boolean }>`
  position: absolute;
  inset: 0;
  background: rgba(18,18,18,0.1);
  z-index: 40;
  opacity: ${({ open }) => (open ? 1 : 0)};
  pointer-events: ${({ open }) => (open ? "auto" : "none")};
  transition: opacity 0.5s ease;
`;

const TopMenu = styled.div`
  height: 56px;
  display: flex;
  flex-direction: row;
  color: ${theme.colors.grayscale.g600};
  padding: 16px 22px;
  align-items: center;
  gap: 10px;
`;

export default function SideBar({ open, onClose }: { open: boolean; onClose: () => void }) {
  return (
    <>
      <SidebarWrap open={open}>
        <TopMenu>
          <img src={search} alt="" />
          <S.Body1>채팅 검색</S.Body1>
        </TopMenu>
        <TopMenu>
          <img src={pencil} alt="" />
          <S.Body1>새 채팅</S.Body1>
        </TopMenu>

        <AccordionMenu title="나의 순간">
          {/* 나의 순간에 들어갈 내용 (리스트, 버튼 등) */}
          <button>
            <S.Body1>
              밤 산책 감성
            </S.Body1>
          </button>
          <button>
            <S.Body1>
              독서 중 재즈 추천
            </S.Body1>
          </button>
          
        </AccordionMenu>

        <AccordionMenu title="공간의 순간">
          {/* 공간의 순간에 들어갈 내용 */}
          <button>
            <S.Body1>
              오늘의 용산 감성
            </S.Body1>
          </button>
          <button>
            <S.Body1>
              종로 청계천 분위기
            </S.Body1>
          </button>
        </AccordionMenu>
      </SidebarWrap>

      <Dimmed open={open} onClick={onClose} />
    </>
  );
}
