import styled from "styled-components";

interface Props {
  children: React.ReactNode;
  backgroundColor?: string;
}

const AppLayout = ({ children, backgroundColor }: Props) => {
  return <Wrapper backgroundColor={backgroundColor}>{children}</Wrapper>;
};

export default AppLayout;

const Wrapper = styled.div<{ backgroundColor?: string }>`
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: ${({ backgroundColor, theme }) =>
    backgroundColor || theme.colors.white};
  width: 393px; /* iPhone 14 width */
  height: 852px; /* iPhone 14 height */
  margin: 0 auto;
  border-radius: 30px;
  box-shadow: 0 0 20px rgba(0, 0, 0, 0.1);
  overflow: hidden;
`;