# <img width="30" height="30" alt="icon vector" src="https://github.com/user-attachments/assets/37498afe-2230-4dcb-a35a-f38947c80913" /> WINK  
### 공간 기반 멀티모달 AI 음악 경험 플랫폼  
**사용자의 ‘순간’을 이해하는 지능적 음악 동반자**

---

## 📌 프로젝트 개요

**WINK**는 단순한 음악 추천을 넘어  
사용자가 처한 **순간(mood + scene + place)** 전체를 AI가 해석해 음악을 제안하는  
**공간 기반 멀티모달 AI 음악 경험 플랫폼**입니다.

텍스트·이미지·위치 데이터를 결합해 사용자가 “지금 이 순간”을 기록하면,  
AI는 이를 감각적으로 해석해 음악을 큐레이션합니다.

본 프로젝트는  
Frontend(React), Backend(Spring Boot), AI Server(Python)가 유기적으로 결합한  
팀 단위 멀티모달 통합 프로젝트이며, Gemma3 멀티모달 모델 + Jamendo RAG + Gemini 기반 요약 모델을 사용하여  
실제 사용자 흐름이 완전히 구현되는 수준까지 구축되었습니다.

---

## 👥 팀원 소개

| 역할 | 담당자 |
|-------|---------|
| **AI** | 한은정 |
| **백엔드** | 박다은 |
| **프론트엔드 & 디자인** | 최예인 | 

---

## 🎯 주요 기능

### 멀티모달 입력 분석
- 텍스트 + 이미지(Base64) + 위치 정보 조합
- AI가 순간의 분위기·장면·의미를 해석

### AI 추천 파이프라인
- 이미지 캡션 생성  
- 텍스트/이미지 통합 문장 생성  
- 핵심 키워드 추출  
- Jamendo RAG 기반 음악 추천

### 순간 기반 세션 시스템
- 세션별 대화 기록  
- 최신 세션은 전체 대화 노출  
- 종료 세션은 요약본 + 추천 음악만 제공

---

## 🏗️ 기술 스택

### **Frontend**
- React + TypeScript + Vite  
- Figma 기반 UX/UI 설계  
- Base64 이미지 인코딩  
- 세션 기반 라우팅 구조  

### **Backend**
- Spring Boot + MySQL  
- 세션/메시지 관리 모델링  
- Base64 이미지 처리 → 로컬 임시 파일 변환  
- Gemini API 기반 요약/재작성  
- 카카오 로컬 API 연동  
- AI 서버와 JSON 표준화 연동  

### **AI Server**
- Python + FastAPI  
- Ollama Gemma3 27B/4B 멀티모달 모델  
- 이미지 캡션 + 텍스트 분석  
- Jamendo 태그 기반 RAG 검색  
- 번역/병합/추론/검색 에이전트 체인

---

## 📹 시연 영상

---
