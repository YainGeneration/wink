# active_session.json 현재 대화 이력 기억
# -*- coding: utf-8 -*-
"""
context_manager.py
- 'active_session.json' 파일을 읽어옵니다.
- Agent 3가 키워드를 추출할 때 참고할 수 있도록,
  '전체 대화 이력'을 하나의 문자열로 생성하여 제공합니다.
"""

import os
import json

def get_full_conversation_history(session_file: str) -> str:
    if not os.path.exists(session_file):
        return ""

    try:
        with open(session_file, "r", encoding="utf-8") as f:
            data = json.load(f)

        merged = data.get("merged_sentence", [])
        keywords = data.get("english_keywords", [])

        if not merged:
            return ""

        history_summary = "User Intent Evolution:\n"

        for i, sentence in enumerate(merged):
            line = f"{i+1}) {sentence}"
            if i < len(keywords) and keywords[i]:
                kw = ", ".join(keywords[i])
                line += f" (keywords: {kw})"
            history_summary += line + "\n"

        return history_summary.strip()

    except:
        return ""

# (RAG 검색, 노래 추천 등 나머지 함수는 모두 삭제)