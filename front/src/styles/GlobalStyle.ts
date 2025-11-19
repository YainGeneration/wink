// 전역 스타일(body, font 등)
// src/styles/GlobalStyle.ts
import { createGlobalStyle } from "styled-components";
import reset from "styled-reset";

const GlobalStyle = createGlobalStyle`
    ${reset}

    *, *::before, *::after {
        box-sizing: border-box;
        margin: 0;
        padding: 0;
    }

    @font-face {
        font-family: 'Pretendard';
        src: url('https://cdn.jsdelivr.net/gh/projectnoonnu/pretendard@1.0/Pretendard-Thin.woff2') format('woff2');
    }

    html, body {
        width: 100%;
        height: 100%;
        background-color: ${({ theme }) => theme.colors.white};
        display: flex;
        justify-content: center;
    }

    #root {
        width: 393px;
        height: 852px;
    }

    a {
        text-decoration: none;
        color: inherit;
    }

    button {
        background: none;
        border: none;
        cursor: pointer;
    }

    /* 스크롤바 커스텀 */
    ::-webkit-scrollbar {
        width: 6px;
    }
    ::-webkit-scrollbar-thumb {
        background-color: ${({ theme }) => theme.colors.grayscale.g200};
        border-radius: 4px;
    }
`;

export default GlobalStyle;
