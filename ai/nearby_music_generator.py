# nearby_music_generator.py
# all-miniLM-L6-v2 사용
# -*- coding: utf-8 -*-
"""
주변 사용자 노래를 Jamendo RAG DB에서 무작위로 5곡 선택하여
JSON 형식으로 반환 및 저장하는 스크립트.
mood_tags가 포함된 곡만 선택하도록 필터링 적용.
"""

import os
import json
import random

from agents.rag_retriever import get_vector_db   # Jamendo RAG DB 로드 함수

SAVE_PATH = "nearby_users.json"


def generate_random_nearby_users(n=5):
    """
    Jamendo RAG DB에서 mood_tags가 존재하는 곡만 무작위로 n개 선택하여
    주변 사용자들이 듣는 음악처럼 JSON 생성.
    """

    db = get_vector_db()

    # 전체 메타데이터 가져오기
    all_docs = db.get()
    metas = all_docs["metadatas"]

    # 🎯 mood_tags가 존재하는 곡만 필터링
    filtered = [m for m in metas if m.get("mood_tags") not in (None, "", " ")]
    count_filtered = len(filtered)

    if count_filtered == 0:
        raise ValueError("❌ mood_tags를 가진 곡이 RAG DB에 없습니다.")

    print(f"📌 mood_tags 포함된 곡 수: {count_filtered}")

    # 선택 개수 조정
    if count_filtered < n:
        print(f"⚠️ mood_tags 곡이 {count_filtered}개뿐 → 전부 반환")
        n = count_filtered

    selected = random.sample(filtered, n)

    result = []
    for meta in selected:
        result.append({
            "title": meta.get("track_name", "Unknown"),
            "artist": meta.get("artist_name", "Unknown"),
            "songId": meta.get("track_id", "")
        })

    # JSON 저장
    with open(SAVE_PATH, "w", encoding="utf-8") as f:
        json.dump(result, f, ensure_ascii=False, indent=2)

    print(f"🎉 주변 사용자 음악 {n}곡 생성 완료 → {SAVE_PATH}")
    return result


if __name__ == "__main__":
    generate_random_nearby_users(5)