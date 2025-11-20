// src/styles/styled.d.ts
import "styled-components";

declare module "styled-components" {
  export interface DefaultTheme {
    colors: {
      primary: string;
      primary_light: string;
      primary_dark: string;
      info: string;
      success: string;
      warning: string;
      error: string;
      white: string;
      black: string;
      black_light: string;
      grayscale: {
        g50: string;
        g100: string;
        g200: string;
        g400: string;
        g500: string;
        g600: string;
        g800: string;
        g900: string;
      };
    };
    fontSize: {
        h1: string;
        h2: string;
        h3: string;
        b1: string;
        b2: string;
        cp: string;
        st: string;
    };
    fontWeight: {
        h1: string;
        h2: string;
        h3: string;
        b1: string;
        b2: string;
        cp: string;
        st: string;
    };
    shadow: {
      default: string;
      pin: string;
    };
  }
}
