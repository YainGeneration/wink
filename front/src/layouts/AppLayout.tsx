import styled from "styled-components";
import { useState } from "react";
import SideBar from "../components/SideBar";
import BaseLayout from "./BaseLayout";
import theme from "../styles/theme";
import { useLocation } from "react-router-dom";
import React from "react";

interface Props {
  children: React.ReactNode;
  // backgroundColor?: string;
}

const routeBackgroundMap: Record<string, string> = {
  "/recommend": theme.colors.black,     // Recommend = 검정
  "/story": theme.colors.grayscale.g50,         // Story = 회색
  "/history": theme.colors.grayscale.g50
};



const AppLayout = ({ children }: Props) => {

  const { pathname } = useLocation();

  // prefix 매칭: "/history"는 "/history/123" 모두 매칭됨
  const matchedKey = Object.keys(routeBackgroundMap).find((key) =>
    pathname.startsWith(key)
);

  // pathname에 맞는 색 선택 (없으면 기본 흰색s)s
  const backgroundColor = matchedKey
    ? routeBackgroundMap[matchedKey]
    : theme.colors.white;
  
  return (
    <Wrapper>
      {React.cloneElement(children as any, { backgroundColor })}
    </Wrapper>
  ); 
};

export default AppLayout;

const Wrapper = styled.div<{ backgroundColor?: string }>`
  display: flex;
  justify-content: center;
  align-items: center;
  width: 393px; /* iPhone 14 width */
  height: 852px; /* iPhone 14 height */
  margin: 0 auto;
  box-shadow: 0 0 20px 0px rgba(0, 0, 0, 0.1);
  // overflow-y: auto;
  // overflow-x: hidden;
  overflow: hidden;
  margin-top: 40px;
`;