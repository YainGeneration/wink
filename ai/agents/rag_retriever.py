# -*- coding: utf-8 -*-
"""
rag_retriever.py
- ChromaDB 로드 및 RAG 기반 음악 추천 기능
- Jamendo API 호출 제거 (DB 정보만 사용)
"""

import os
import json
import numpy as np
from langchain_community.vectorstores import Chroma
from langchain_huggingface import HuggingFaceEmbeddings


# =========================================================
# 1. 경로 설정
# =========================================================
SCRIPT_DIR = os.path.dirname(__file__)       # .../ai/rag
PROJECT_ROOT = os.path.dirname(SCRIPT_DIR)   # .../ai

# 임베딩 설정 다시 보기
DB_PERSIST_DIR = os.path.join(PROJECT_ROOT, "rag/chroma_db_all-MiniLM-l6-v2")
# DB_PERSIST_DIR = os.path.join(PROJECT_ROOT, "rag/chroma_db_all-mpnet-base-v2")
COLLECTION_NAME = "jamendo_songs"
EMBED_MODEL_NAME = "all-MiniLM-l6-v2"
# EMBED_MODEL_NAME = "all-mpnet-base-v2"

# =========================================================
# 2. 전역 캐시
# =========================================================
_vector_db = None
_embedding_fn = None


# =========================================================
# 3. DB 및 임베딩 모델 로드
# =========================================================
def _load_retriever_resources():
    """ChromaDB + Embedding 모델 캐시 로드."""
    global _vector_db, _embedding_fn

    if _vector_db is not None and _embedding_fn is not None:
        return _vector_db, _embedding_fn

    print(f"🚀 [RAG] Loading Embedding Model: {EMBED_MODEL_NAME}")
    _embedding_fn = HuggingFaceEmbeddings(
        model_name=EMBED_MODEL_NAME,
        encode_kwargs={"normalize_embeddings": True},
    )

    print(f"🚀 [RAG] Loading ChromaDB from: {DB_PERSIST_DIR}")
    if not os.path.exists(DB_PERSIST_DIR):
        raise FileNotFoundError(f"[RAG] DB directory not found: {DB_PERSIST_DIR}")

    _vector_db = Chroma(
        persist_directory=DB_PERSIST_DIR,
        collection_name=COLLECTION_NAME,
        embedding_function=_embedding_fn,
    )

    print(f"✅ [RAG] DB Loaded: Collection '{COLLECTION_NAME}' Ready")
    return _vector_db, _embedding_fn


# =========================================================
# 각 키워드 별 유사도 계산 위해 코사인 계산 함수 사용
# =========================================================
def cosine_sim(a, b):
    return float(np.dot(a, b))



# =========================================================
# 4. 추천 결과 metadata 반환 (추가 API 호출 없음)
# =========================================================
def enrich_song_metadata(metadata: dict) -> dict:
    """그대로 반환 (DB에 이미 필요한 정보가 모두 있음)."""
    return metadata



# =========================================================
# 3. 고급 RAG 검색
# =========================================================
def get_song_recommendations(english_keywords: list[str], top_k: int = 5):
    vector_db, embed = _load_retriever_resources()

    # 키워드 개별 임베딩
    keyword_vecs = [embed.embed_query(k) for k in english_keywords]

    # 키워드를 하나의 문장으로 합쳐서 문장 임베딩도 계산
    # 문장 가공 없이 연달아 연결할 뿐, 문장 생성이 아님
    full_query_vec = embed.embed_query(" ".join(english_keywords))

    # ============================
    # B. 후보 노래 50개 추출
    # ============================
    raw_results = vector_db.similarity_search_with_score(" ".join(english_keywords), k=80)

    # ============================
    # C. 고급 점수 계산
    # ============================
    scored = []

    for doc, base_score in raw_results:
        meta = doc.metadata

        song_text = f"{meta['genre_tags']} {meta['mood_tags']}"
        song_vec = embed.embed_query(song_text)

        # 1) 키워드별 cosine similarity 평균
        indiv_sims = [
            cosine_sim(song_vec, kvec) for kvec in keyword_vecs
        ]
        indiv_sim_mean = float(np.mean(indiv_sims))

        # 2) genre/mood 분리 similarity
        genre_vec = embed.embed_query(meta["genre_tags"])
        mood_vec = embed.embed_query(meta["mood_tags"])

        genre_sim = cosine_sim(genre_vec, full_query_vec)
        mood_sim = cosine_sim(mood_vec, full_query_vec)

        genre_weight = 0.4
        mood_weight = 0.6
        gm_score = genre_sim * genre_weight + mood_sim * mood_weight

        # 3) 태그 직접 매칭 보정
        tag_bonus = sum([
            0.05 if kw.lower() in song_text.lower() else 0
            for kw in english_keywords
        ])

        # 4) 총합 점수
        final_score = indiv_sim_mean * 0.4 + gm_score * 0.6 + tag_bonus

        meta_copy = meta.copy()
        meta_copy["similarity_score"] = float(final_score)

        scored.append(meta_copy)

    # D. 앨범 중복 제거 (가장 높은 점수만)
    album_best = {}
    for m in scored:
        album = m.get("album_name", "Unknown")
        if album not in album_best or m["similarity_score"] > album_best[album]["similarity_score"]:
            album_best[album] = m

    # E. 정렬 후 top_k 선택
    sorted_final = sorted(
        album_best.values(),
        key=lambda x: x["similarity_score"],
        reverse=True
    )[:top_k]

    return sorted_final

# 외부에서 RAG DB 접근 시 사용
def get_vector_db():
    """외부에서 RAG DB를 직접 사용하도록 반환."""
    vector_db, _ = _load_retriever_resources()
    return vector_db

# 문장 임베딩하여 벡터 변환
def embed_text(text: str):
    """문장을 임베딩하여 벡터 반환."""
    _, embed = _load_retriever_resources()
    return embed.embed_query(text)


# =========================================================
# 6. 단독 실행 테스트
# =========================================================
if __name__ == "__main__":
    print("--- RAG Retriever Test ---")

    test1 = ["angry", "rock", "metal"]
    recs1 = get_song_recommendations(test1, top_k=3)
    print("\n[Test 1]")
    print(json.dumps(recs1, indent=2, ensure_ascii=False))

    test2 = ["gentle", "soft", "melodic"]
    recs2 = get_song_recommendations(test2, top_k=3)
    print("\n[Test 2]")
    print(json.dumps(recs2, indent=2, ensure_ascii=False))
