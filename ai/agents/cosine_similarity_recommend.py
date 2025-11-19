# audio_feature_embeddings.json의 정규화된 임베딩 값
# user_keyword_embedding_session_weighted.npy의 정규화된 임베딩 값
# 두 개의 임베딩 값으로 코사인 유사도 비교
# 추천까지

import os
import json
import numpy as np
import pandas as pd
from datetime import datetime
import spotipy
from spotipy.oauth2 import SpotifyOAuth

sp = spotipy.Spotify(auth_manager=SpotifyOAuth(
    client_id='9f601ae991474c5f9acbbca99f0d9c7c',
    client_secret='302529b448714aaabc311bdb65772a96',
    redirect_uri='http://127.0.0.1:8888/callback',
    scope="user-library-read"
))


# -------------------------------
# 경로 설정
# -------------------------------
SESSION_DIR = "agents/keywords"
EMBED_DIR = "spotify/embedding_data"
AUDIO_FEATURE_EMB_FILE = os.path.join(EMBED_DIR, "audio_feature_embeddings.json")

# Spotify 최종 클린 데이터 (오디오 피처 포함)
SONG_DATA_FILE = "spotify/data/merged_audio_features_after2000_final.csv"

# 추천에 사용할 피처 목록
AUDIO_FEATURE_COLUMNS = [
    "danceability", "instrumentalness", "valence",
     "loudness",  "speechiness"
]


# =========================================================
# 0) 최신 세션 선택
# =========================================================
def get_latest_session_file():
    candidates = []
    for fname in os.listdir(SESSION_DIR):
        if fname.startswith("session_") and fname.endswith(".json"):
            path = os.path.join(SESSION_DIR, fname)
            try:
                data = json.load(open(path, "r", encoding="utf-8"))
                if "session_end" in data:
                    t = datetime.strptime(data["session_end"], "%Y-%m-%d %H:%M:%S")
                    candidates.append((t, path))
            except:
                continue
    if not candidates:
        raise FileNotFoundError("❌ session_end 포함된 세션 파일 없음")
    return max(candidates, key=lambda x: x[0])[1]


def extract_session_id(path):
    return os.path.basename(path).replace("session_", "").replace(".json", "")


# 현재 활성 세션 파일 경로
def get_active_session_id():
    active_path = os.path.join(SESSION_DIR, "active_session.json")
    if not os.path.exists(active_path):
        raise FileNotFoundError("❌ active_session.json 파일이 없음")
    
    with open(active_path, "r", encoding="utf-8") as f:
        data = json.load(f)

    return data.get("session_id")

# =========================================================
# 1) 정규화 함수
# =========================================================
def l2_norm(v):
    v = np.array(v, dtype=np.float32)
    n = np.linalg.norm(v)
    return v / n if n > 0 else v


# =========================================================
# 2) 사용자 임베딩 로드 (weighted / latest 자동)
# =========================================================
def load_user_embedding(mode="weighted"):
    session_id = get_active_session_id()

    fname = f"user_keyword_embedding_{session_id}_{mode}.npy"
    fpath = os.path.join(EMBED_DIR, fname)

    if not os.path.exists(fpath):
        raise FileNotFoundError(f"❌ 사용자 임베딩 없음: {fpath}")

    emb = np.load(fpath)
    return l2_norm(emb), session_id


# =========================================================
# 3) 오디오 피처 설명 임베딩 로드
# =========================================================
def load_audio_feature_embeddings():
    data = json.load(open(AUDIO_FEATURE_EMB_FILE, "r", encoding="utf-8"))
    return {k: l2_norm(v) for k, v in data.items()}


# =========================================================
# 4) 코사인 유사도
# =========================================================
def cosine(a, b):
    return float(np.dot(a, b))


# =========================================================
# 5) feature 유사도 계산
# =========================================================
def compute_feature_similarities(user_emb):
    feature_embs = load_audio_feature_embeddings()
    sims = {feat: cosine(user_emb, emb) for feat, emb in feature_embs.items()}
    sims = dict(sorted(sims.items(), key=lambda x: x[1], reverse=True))
    return sims


# =========================================================
# 6) Spotify 곡 데이터 로드
# =========================================================
def load_song_data():
    if not os.path.exists(SONG_DATA_FILE):
        raise FileNotFoundError(f"❌ 노래 데이터 파일 없음: {SONG_DATA_FILE}")

    df = pd.read_csv(SONG_DATA_FILE)
    
    # 1. 결측치 제거
    df = df.dropna(subset=AUDIO_FEATURE_COLUMNS)

    # 2. [NEW] 중복 곡 제거 (가수 이름 + 노래 제목이 같으면 중복으로 처리)
    # keep='first': 중복된 것 중 첫 번째만 남기고 나머지는 버림
    original_len_dup = len(df)
    df = df.drop_duplicates(subset=['artist_name', 'track_name'], keep='first')
    print(f"✂️ [Deduplication] Removed {original_len_dup - len(df)} duplicate tracks.")

    return df    
    
# =========================================================
# 스포티파이 앨범 커버 이미지 가져오기
def get_album_cover_url(track_id):
    try:
        track = sp.track(track_id)
        images = track["album"]["images"]
        if images:
            return images[0]["url"]
        return None
    except Exception as e:
        print(f"❌ Failed to fetch album cover for {track_id}: {e}")
        return None
    
# =========================================================
# 스포티파이 미리듣기 30초 URL 가져오기
def get_preview_url(track_id):
    try:
        track = sp.track(track_id)
        return track.get("preview_url", None)
    except Exception as e:
        print(f"❌ Failed to fetch preview_url for {track_id}: {e}")
        return None


# =========================================================
# 7) 최종 점수 계산
#   score(song_i) = Σ( sim(feature_j) × song_i[feature_j] )
# =========================================================
def compute_recommendation_scores(df, feature_sims):
    df = df.copy()
    
    # 피처 정규화를 위한 min-max 계산
    min_vals = df[AUDIO_FEATURE_COLUMNS].min()
    max_vals = df[AUDIO_FEATURE_COLUMNS].max()
    
    # 정규화된 피처 만들기
    norm_features = (df[AUDIO_FEATURE_COLUMNS] - min_vals) / (max_vals - min_vals)
    norm_features = norm_features.fillna(0)     # 혹시 모를 division by zero 대비
    
    scores = []

    for i, row in df.iterrows():
        score = 0.0

        for feat in AUDIO_FEATURE_COLUMNS:
            feature_value = norm_features.loc[i, feat]
            sim = feature_sims.get(feat, 0.0)
            score += sim * feature_value

        scores.append(score)

    df["recommend_score"] = scores
    return df


# =========================================================
# 8) 추천 실행
# =========================================================
def recommend(top_k=5, mode="weighted"):

    # 1) 사용자 임베딩 로드
    user_emb, session_id = load_user_embedding(mode)

    print(f"\n🧠 최신 세션 ID: {session_id}")
    print(f"🎯 임베딩 모드: {mode}")

    # 2) feature 유사도 계산
    feature_sims = compute_feature_similarities(user_emb)

    print("\n📊 오디오 피처 유사도:")
    for k, v in feature_sims.items():
        print(f"{k:15s} : {v:.10f}")

    # 3) 곡 데이터 로드
    df = load_song_data()

    # 4) 최종 점수 계산
    scored_df = compute_recommendation_scores(df, feature_sims)

    # 상위 곡 5개 선택
    rec = scored_df.sort_values("recommend_score", ascending=False).head(top_k).copy()
    # ★ 앨범 커버 URL 추가
    rec["album_cover_url"] = rec["id"].apply(get_album_cover_url)
    
    print("\n🎵 추천 결과 (Top 5):")
    print(rec[["id", "track_name", "artist_name", "recommend_score"]])

    return rec


# =========================================================
# 9) 실행
# =========================================================
if __name__ == "__main__":
    recommend(top_k=5, mode="weighted")
