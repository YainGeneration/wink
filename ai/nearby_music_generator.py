import os
import json
import random

from agents.rag_retriever import get_vector_db   # Jamendo RAG DB 로드 함수

SAVE_PATH = "agents/nearby_users.json"

# 🔥 이미지 경로 & 위치 정보는 완전 고정
FIXED_IMAGE_PATH = "/Users/eunjung/Desktop/wink/ai/images/snow.JPG"
FIXED_LOCATION = {
    "lat": 37.55,
    "lng": 126.97,
    "address": "서울시 용산구 한강대로",
    "placeName": "홍순언 앞 거리"
}


def generate_random_nearby_users(n=5):
    """
    Jamendo RAG DB에서 mood_tags가 존재하는 곡만 무작위로 n개 선택하여 저장.
    """

    db = get_vector_db()

    # 전체 메타데이터 가져오기
    all_docs = db.get()
    metas = all_docs["metadatas"]

    # mood_tags가 존재하는 곡만 필터링
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

    nearby_music_list = []
    for meta in selected:
        nearby_music_list.append({
            "title": meta.get("track_name", "Unknown"),
            "artist": meta.get("artist_name", "Unknown"),
            "songId": meta.get("track_id", "")
        })

    # 🔥 최종 JSON 구조 구성
    output_json = {
        "imagePath": FIXED_IMAGE_PATH,
        "location": FIXED_LOCATION,
        "nearbyMusic": nearby_music_list
    }

    # JSON 저장
    with open(SAVE_PATH, "w", encoding="utf-8") as f:
        json.dump(output_json, f, ensure_ascii=False, indent=2)

    print(f"🎉 주변 사용자 음악 {n}곡 생성 완료 → {SAVE_PATH}")
    return output_json


if __name__ == "__main__":
    generate_random_nearby_users(5)