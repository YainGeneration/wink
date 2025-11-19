import os
import json
import torch
from datetime import datetime
from transformers import AutoTokenizer, AutoModel
import numpy as np

MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"
SESSION_DIR = "agents/keywords"
EMBED_SAVE_DIR = "spotify/embedding_data"
os.makedirs(EMBED_SAVE_DIR, exist_ok=True)

# -----------------------------
# 1) 모델 로드
# -----------------------------
device = "cuda" if torch.cuda.is_available() else "cpu"

_tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
_model = AutoModel.from_pretrained(MODEL_NAME)


# -----------------------------
# L2 정규화
# -----------------------------
def l2_normalize(vec: np.ndarray):
    norm = np.linalg.norm(vec)
    if norm == 0:
        return vec
    return vec / norm


# -----------------------------
# 2) 텍스트 임베딩 생성
# -----------------------------
def get_text_embedding(text: str):
    inputs = _tokenizer(text, return_tensors="pt", truncation=True, padding=True).to(device)

    with torch.no_grad():
        outputs = _model(**inputs, output_hidden_states=True)

    hidden = outputs.hidden_states[-1]     # 마지막 레이어
    embedding = hidden.mean(dim=1).squeeze()

    embedding = embedding.cpu().numpy().astype(np.float32)
    embedding = l2_normalize(embedding)
    return embedding


# -----------------------------
# 3) active_session.json 읽기
# -----------------------------
def get_active_session_path():
    path = os.path.join(SESSION_DIR, "active_session.json")
    if not os.path.exists(path):
        raise FileNotFoundError("❌ active_session.json 없음")
    return path


def get_active_session_id():
    path = get_active_session_path()
    with open(path, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data.get("session_id")


# -----------------------------
# 4) 최신 키워드 임베딩 (active 기반)
# -----------------------------
def get_user_keyword_embedding_active():
    session_path = get_active_session_path()

    with open(session_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    keywords_all = data.get("english_keywords", [])
    if not keywords_all:
        raise ValueError("english_keywords가 active_session.json에 없음")

    latest_keywords = keywords_all[-1]
    merged_text = " ".join(latest_keywords)

    print(f"🧠 Active 세션: {session_path}")
    print(f"📝 최신 키워드: {latest_keywords}")

    return get_text_embedding(merged_text)


# -----------------------------
# 5) 가중 평균 임베딩 (active 기반)
# -----------------------------
def get_weighted_user_embedding_active():
    session_path = get_active_session_path()
    with open(session_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    all_keyword_sets = data.get("english_keywords", [])
    if not all_keyword_sets:
        raise ValueError("english_keywords not found in active_session.json")

    embeddings = []
    weights = []
    n = len(all_keyword_sets)

    for i, kw_list in enumerate(all_keyword_sets):
        text = " ".join(kw_list)
        emb = get_text_embedding(text)
        embeddings.append(emb)

        # 최신 키워드일수록 가중치 ↑
        w = 0.5 + 0.5 * (i / (n - 1)) if n > 1 else 1.0
        weights.append(w)

    embeddings = np.vstack(embeddings)
    weights = np.array(weights).reshape(-1, 1)

    weighted_vec = (embeddings * weights).sum(axis=0) / weights.sum()
    weighted_vec = l2_normalize(weighted_vec)
    
    return weighted_vec.astype(np.float32)


# -----------------------------
# 6) 임베딩 저장
# -----------------------------
def save_embedding(embedding: np.ndarray, session_id: str, mode: str = "latest"):
    fname = f"user_keyword_embedding_{session_id}_{mode}.npy"
    path = os.path.join(EMBED_SAVE_DIR, fname)
    np.save(path, embedding)
    print(f"💾 Saved {mode} embedding → {path}")


# -----------------------------
# 7) 실행 (테스트 용)
# -----------------------------
if __name__ == "__main__":
    session_id = get_active_session_id()

    # 최신 키워드 임베딩
    latest_emb = get_user_keyword_embedding_active()
    print("📌 최신 임베딩 shape:", latest_emb.shape)
    save_embedding(latest_emb, session_id, mode="latest")

    # 가중 평균 임베딩
    weighted_emb = get_weighted_user_embedding_active()
    print("📌 가중 평균 임베딩 shape:", weighted_emb.shape)
    save_embedding(weighted_emb, session_id, mode="weighted")
