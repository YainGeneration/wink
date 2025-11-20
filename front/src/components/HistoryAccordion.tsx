import { useState, useRef, useEffect } from "react";
import styled from "styled-components";

import chevronDown from "../assets/icons/chevron-down-g500.svg";
import chevronUp from "../assets/icons/chevron-up-g500.svg";
import theme from "../styles/theme";
import S from "../styles/styled";

interface AccordionProps {
  title: string;
  subtitle?: React.ReactNode;
  children: React.ReactNode;
  defaultOpen?: boolean;
}

export default function HistoryAccordion({
  title,
  subtitle,
  children,
  defaultOpen = false,
}: AccordionProps) {
  const [open, setOpen] = useState(defaultOpen);
  const contentRef = useRef<HTMLDivElement>(null);
  const [height, setHeight] = useState("10px");

  useEffect(() => {
    if (open) {
      setHeight(`${contentRef.current?.scrollHeight}px`);
    } else {
      setHeight("0px");
    }
  }, [open]);

  return (
    <Card>
      <Header onClick={() => setOpen(!open)}>
        <TextBlock>
          <Title>
            <S.Heading3>{title}</S.Heading3>
          </Title>
          {subtitle && <Subtitle><S.Body2>{subtitle}</S.Body2></Subtitle>}
        </TextBlock>

        <Icon src={open ? chevronUp : chevronDown} alt="arrow" />
      </Header>

      <ContentWrapper style={{ height }}>
        <Inner ref={contentRef}>{children}</Inner>
      </ContentWrapper>
    </Card>
  );
}

// ------------------- styles -------------------

const Card = styled.div`
  background-color: ${theme.colors.white};
  padding: 12px 16px 14px;
  margin-top: 16px;
  border-radius: 10px;
  box-shadow: ${theme.shadow.default};
`;

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  cursor: pointer;
`;

const TextBlock = styled.div`
  display: flex;
  flex-direction: column;
`;

const Title = styled.div`
  color: ${theme.colors.grayscale.g600};
  margin-bottom: 8px;
`;

const Subtitle = styled.div`
  font-size: 13px;
  color: ${theme.colors.grayscale.g600};
  margin-top: 4px;
`;

const Icon = styled.img`
  width: 20px;
  height: 20px;
`;

const ContentWrapper = styled.div`
  overflow: hidden;
  transition: height 0.5s ease;
`;

const Inner = styled.div`
  padding-top: 12px;
`;
