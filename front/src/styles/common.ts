// 자주 쓰는 styled 컴포넌트 모음
import type { Http2ServerResponse } from "node:http2";
import { styled, css } from "styled-components";

export const flexAllCenterColumn = css`
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
`;
export const flexAllCenterRow = css`
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
`;
export const flexAllCenter = css`
  display: flex;
  justify-content: center;
  align-items: center;
`;

export const typography = {
  h1: css`
    font-size: ${({theme}) => theme.fontSize.h1};
    font-weight: ${({theme}) => theme.fontWeight.h1};
    line-height: 36px;
    letter-spacing: -0.005em;
  `,
  h2: css`
    font-size: ${({theme}) => theme.fontSize.h2};
    font-weight: ${({theme}) => theme.fontWeight.h2};
    line-height: 32px;
    letter-spacing: -0.003em;
  `,
  h3: css`
    font-size: ${({theme}) => theme.fontSize.h3};
    font-weight: ${({theme}) => theme.fontWeight.h3};
    line-height: 28px;
    letter-spacing: -0.002em;
  `,
  b1: css`
    font-size: ${({theme}) => theme.fontSize.b1};
    font-weight: ${({theme}) => theme.fontWeight.b1};
    line-height: 24px;
    letter-spacing: 0em;
  `,
  b2: css`
    font-size: ${({theme}) => theme.fontSize.b2};
    font-weight: ${({theme}) => theme.fontWeight.b2};
    line-height: 20px;
    letter-spacing: 0em;
  `,
  cp: css`
    font-size: ${({theme}) => theme.fontSize.cp};
    font-weight: ${({theme}) => theme.fontWeight.cp};
    line-height: 16px;
    letter-spacing: 0.001em;
  `,
  st: css`
    font-size: ${({theme}) => theme.fontSize.st};
    font-weight: ${({theme}) => theme.fontWeight.st};
    line-height: 14px;
    letter-spacing: 0.002em;
  `,
};


export const flexRow = css`
  display: flex;
  flex-direction: row;
`;

export const flexColumn = css`
  display: flex;
  flex-direction: column;
`;