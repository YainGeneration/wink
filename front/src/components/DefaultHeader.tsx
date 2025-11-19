import React, { useState } from 'react';
import styled from 'styled-components';
import alignJustified from "./../assets/icons/align-justified.svg";
import bell from "./../assets/icons/bell.svg";

const Wrapper = styled.div`
  display: flex;
  height: 58px;
  margin-top: 44px;
  padding: 16px 0px;
  justify-content: space-between;
  background-color: transparent;
`;

const DefaultHeader = ({ onMenuClick }: { onMenuClick: () => void }) => {
    console.log(onMenuClick)
    return (
        <Wrapper>
            <button onClick={onMenuClick}>
                <img src={alignJustified} alt="" />
            </button>
            <button>
                <img src={bell} alt="" />
            </button>
        </Wrapper>
    );
};

export default DefaultHeader;