// color, fontsize, shadow등 공통 변수
// src/styles/theme.ts
import type { DefaultTheme } from "styled-components";

const theme: DefaultTheme = {
  colors: {
      primary: "#FA4EAB",
      primary_light: "#FDE3F1",
      primary_dark: "#FB0093",
      info: "#3385FF",
      success: "#00E581",
      warning: "#FFDB4D",
      error: "#FF584D",
      white: "#FFFFFF",
      black: "#121212",
      black_light: "#222222",
      grayscale: {
        g50: "#F7F8F9",
        g100: "#E8EBED",
        g200: "#C9CDD2",
        g400: "#9EA4AA",
        g500: "#72787F",
        g600: "#454C53",
        g800: "#26282B",
        g900: "#1B1D1F",
      },
    },
    fontSize: {
        h1: "28px",
        h2: "24px",
        h3: "20px",
        b1: "16px",
        b2: "14px",
        cp: "12px",
        st: "11px"
    },
    fontWeight: {
        h1: "700",
        h2: "600",
        h3: "600",
        b1: "400",
        b2: "400",
        cp: "500",
        st: "500",
    },
    shadow: {
      default: "0px 0px 20px 0px rgba(0, 0, 0, 0.05)",
      pin: "0px 0px 10px 0px rgba(0, 0, 0, 0.3)",
    },
};

export default theme;
