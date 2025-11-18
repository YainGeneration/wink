import React from 'react';
import styled from 'styled-components';
import alignJustified from "./../assets/icons/align-justified.svg";
import bell from "./../assets/icons/bell.svg";

const Wrapper = styled.div`
  display: flex;
  height: 58px;
  padding: 16px 0px;
  justify-content: space-between;
`;

const DefaultHeader = () => {
    return (
        <Wrapper>
            <button>
                <img src={alignJustified} alt="" />
            </button>
            <button>
                <img src={bell} alt="" />
            </button>
        </Wrapper>
    );
};

export default DefaultHeader;