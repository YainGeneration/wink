import os
import json
import torch
from datetime import datetime
from transformers import AutoTokenizer, AutoModel
import numpy as np

MODEL_NAME = "michellejieli/emotion_text_classifier"
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
# 2) 텍스트 임베딩 함수
# -----------------------------
def get_text_embedding(text: str):
    inputs = _tokenizer(text, return_tensors="pt", truncation=True, padding=True).to(device)

    with torch.no_grad():
        outputs = _model(**inputs, output_hidden_states=True)

    hidden = outputs.hidden_states[-1]     # 마지막 레이어
    embedding = hidden.mean(dim=1).squeeze()

    # CPU로 이동 후 numpy 변환
    embedding = embedding.cpu().numpy().astype(np.float32)

    # 임베딩 정규화
    norm = np.linalg.norm(embedding)
    if norm > 0:
        embedding = embedding / norm

    return embedding


# -----------------------------
# 3) 가장 최신 session_end를 가진 JSON 찾기
# -----------------------------
def get_latest_session_file_by_endtime():
    candidates = []

    for fname in os.listdir(SESSION_DIR):
        if not fname.startswith("session_") or not fname.endswith(".json"):
            continue
        
        path = os.path.join(SESSION_DIR, fname)

        try:
            with open(path, "r", encoding="utf-8") as f:
                data = json.load(f)
            
            session_end = data.get("session_end", None)
            if session_end:
                end_time = datetime.strptime(session_end, "%Y-%m-%d %H:%M:%S")
                candidates.append((end_time, path))
        except:
            continue

    if not candidates:
        raise FileNotFoundError("session_end를 포함한 세션 파일이 없습니다.")

    # 가장 최근 session_end
    latest_path = max(candidates, key=lambda x: x[0])[1]
    return latest_path


# -----------------------------
# 4) 최신 키워드만 임베딩 추출
# -----------------------------
def get_user_keyword_embedding():
    session_path = get_latest_session_file_by_endtime()

    with open(session_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    keywords_all = data.get("english_keywords", [])
    if not keywords_all:
        raise ValueError("세션 파일에 english_keywords가 없습니다.")

    latest_keywords = keywords_all[-1]  # 가장 마지막 키워드 세트
    merged_text = " ".join(latest_keywords)

    print(f"🧠 최신 세션: {session_path}")
    print(f"📝 최신 키워드: {latest_keywords}")

    return get_text_embedding(merged_text)

# -----------------------------
# 5) 여러 키워드에 가중치 적용 (추천용)
# -----------------------------
def get_weighted_user_embedding():
    session_path = get_latest_session_file_by_endtime()
    with open(session_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    all_keyword_sets = data.get("english_keywords", [])
    if not all_keyword_sets:
        raise ValueError("english_keywords not found in session")

    embeddings = []
    weights = []

    # weight increases linearly for newer entries
    n = len(all_keyword_sets)

    for i, kw_list in enumerate(all_keyword_sets):
        text = " ".join(kw_list)
        emb = get_text_embedding(text)
        embeddings.append(emb)

        # 가중치: 0.5 → ... → 1.0
        w = 0.5 + 0.5 * (i / (n - 1)) if n > 1 else 1.0
        weights.append(w)

    embeddings = np.vstack(embeddings)
    weights = np.array(weights).reshape(-1, 1)

    weighted_vec = (embeddings * weights).sum(axis=0) / weights.sum()
    normalized = weighted_vec / np.linalg.norm(weighted_vec)
    
    return normalized.astype(np.float32)

# -----------------------------
# 6) 세션 id 추출 함수
# -----------------------------
def extract_session_id(session_path: str):
    """
    filename: session_abc123.json → abc123
    """
    fname = os.path.basename(session_path)
    return fname.replace("session_", "").replace(".json", "")

# -----------------------------
# 7) 임베딩 저장 함수
# -----------------------------
def save_embedding(embedding: np.ndarray, session_id: str, mode: str = "latest"):
    """
    embedding: numpy vector
    session_id: "abc123"
    mode: "latest" or "weighted"
    """
    fname = f"user_keyword_embedding_{session_id}_{mode}.npy"
    path = os.path.join(EMBED_SAVE_DIR, fname)
    np.save(path, embedding)
    print(f"💾 Saved {mode} embedding → {path}")


if __name__ == "__main__":
    session_path = get_latest_session_file_by_endtime()
    session_id = extract_session_id(session_path)
    
    # 최신 키워드만
    latest_emb = get_user_keyword_embedding()
    print("📌 최신 임베딩 shape:", latest_emb.shape)
    save_embedding(latest_emb, session_id, mode="latest")

    # 가중 평균 임베딩
    weighted_emb = get_weighted_user_embedding()
    print("📌 가중 평균 임베딩 shape:", weighted_emb.shape)
    save_embedding(weighted_emb, session_id, mode="weighted")
