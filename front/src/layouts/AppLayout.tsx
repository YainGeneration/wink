import styled from "styled-components";

const AppLayout = ({ children }: { children: React.ReactNode }) => {
  return (
    <Wrapper>
        {children}
    </Wrapper>
  );
};

export default AppLayout;

const Wrapper = styled.div`
  display: flex;
  justify-content: center;
  align-items: center;
  background: ${({theme}) => theme.colors.white};
  min-height: 852px;
`;