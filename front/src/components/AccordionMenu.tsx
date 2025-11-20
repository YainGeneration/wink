import { useState } from "react";
import styled, { css } from "styled-components";
import S from "../styles/styled";
import theme from "../styles/theme";
// 화살표 아이콘이 없다면 assets 폴더에 추가하거나, 아래 코드를 수정해 주세요.
import chevronIcon from "../assets/icons/chevron-down-g500.svg"; 

interface AccordionProps {
  title: string;
  children: React.ReactNode;
  defaultOpen?: boolean; // 초기에 열려있게 할지 여부 (선택 사항)
}

const AccordionWrapper = styled.div`
  width: 100%;
  display: flex;
  flex-direction: column;
  /* 필요 시 하단 border 추가 */
  margin-top: 20px;
//   border-bottom: 1px solid ${theme.colors.grayscale.g200};
`;

const Header = styled.div<{ isOpen: boolean }>`
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 22px; /* TopMenu와 유사한 패딩 */
  cursor: pointer;
  transition: background-color 0.2s ease;
  user-select: none;

  &:hover {
    background-color: ${theme.colors.grayscale.g50}; /* 호버 시 연한 배경 */
  }
`;

const Title = styled(S.Body2)`
  color: ${theme.colors.grayscale.g500};
  font-weight: 500;
`;

const Icon = styled.img<{ isOpen: boolean }>`
  width: 20px; /* 아이콘 크기 조절 */
  height: 20px;
  transition: transform 0.5s ease;
  /* 열려있으면 180도 회전 (위쪽 화살표가 됨) */
  transform: ${({ isOpen }) => (isOpen ? "rotate(-180deg)" : "rotate(0deg)")};
  opacity: 0.5; /* 아이콘이 너무 진하지 않게 조정 */
`;

const ContentWrapper = styled.div<{ isOpen: boolean }>`
  overflow: hidden;
  transition: max-height 0.3s ease-in-out, opacity 0.3s ease-in-out;
  
  /* max-height를 사용하여 애니메이션 구현 
    내용이 길어질 수 있다면 500px보다 넉넉하게 잡으세요.
  */
  max-height: ${({ isOpen }) => (isOpen ? "500px" : "0")};
  opacity: ${({ isOpen }) => (isOpen ? "1" : "0")};
`;

const ContentInner = styled.div`
  display: flex;
  flex-direction: column;
  gap: 8px; /* 아이템 간 간격 */

  ${S.Body1} {
    padding: 8px 22px;
    transition: background-color 0.2s ease;
    width: 100%;
    text-align: left;

    &:hover {
      background-color: ${theme.colors.primary_light};
    }
  }

`;

export default function AccordionMenu({ 
  title, 
  children, 
  defaultOpen = false 
}: AccordionProps) {
  const [isOpen, setIsOpen] = useState(defaultOpen);

  const handleToggle = () => {
    setIsOpen((prev) => !prev);
  };

  return (
    <AccordionWrapper>
      <Header onClick={handleToggle} isOpen={isOpen}>
        <Title>{title}</Title>
        <Icon src={chevronIcon} alt="toggle" isOpen={isOpen} />
      </Header>
      
      <ContentWrapper isOpen={isOpen}>
        <ContentInner>
          {children}
        </ContentInner>
      </ContentWrapper>
    </AccordionWrapper>
  );
}