# spotify main pipeline

"""
Agent3 (통합 파이프라인)
- Agent 1: 한국어 → 영어 (EXAONE)
- Agent 2: 이미지 → 영어 캡션 (Gemma3)
- Agent 3: 문장 합성 + 키워드 추출 (Gemma3)
- 추천: 코사인 유사도 기반 Spotify 오디오피처 추천
- 세션 저장: active_session.json
"""

import os
import re
import json
from datetime import datetime
import requests
import uuid

# --------------------------
# Agent 1: 번역 모듈
# --------------------------
try:
    from agent1_exaone import korean_to_english
except ImportError:
    print("❌ 'agent1_exaone.py' 파일을 찾을 수 없습니다.")
    exit()

# --------------------------
# Agent 2: 이미지 캡셔닝
# --------------------------
try:
    from agent2_imageToEng import image_to_english_caption
except ImportError:
    print("❌ 'agent2_imageToEng.py' 파일을 찾을 수 없습니다.")
    exit()

# --------------------------
# Context Manager
# --------------------------
try:
    from context_manager import get_full_conversation_history
except ImportError:
    print("❌ 'context_manager.py' 파일을 찾을 수 없습니다.")
    exit()

# --------------------------
# Cosine Recommender
# --------------------------
try:
    from cosine_similarity_recommend import recommend, get_album_cover_url, get_preview_url
except ImportError:
    print("❌ 'spotify/cosine_similarity_recommend.py' 파일을 찾을 수 없습니다.")
    exit()

# =========================================================
# 전역 설정
# =========================================================
OLLAMA_URL = "http://localhost:11434"
GEMMA3_MODEL = "gemma3:27b"
SAVE_DIR = "agents/keywords"
os.makedirs(SAVE_DIR, exist_ok=True)


# =========================================================
# Agent 3-1: 문장 병합 (Creation X -> Combination O)
# =========================================================
def rewrite_combined_sentence(text1: str, text2: str, full_history: str) -> str:    
    # 입력이 없으면 빈 문자열 반환
    if not text1 and not text2:
        print("⚠️ [Agent 3] No input text provided.")
        return ""

    print("🧩 [Agent 3] Merging sentences (Strict Combination)...")

    # [핵심 변경] '작가'가 아니라 '편집자' 모드로 변경
    # 내용을 새로 쓰지 말고, 두 문장을 자연스럽게 잇기만 하라고 지시
    prompt = f"""
You are a text editor.
Your task is to **logically connect** the User Text and the Visual Context into one clear English sentence.

[Inputs]
1. **User Request (Primary)**: "{text1}"
2. **Visual Context (Secondary)**: "{text2}"
3. **Context History**: "{full_history}"

[Strict Rules]
- **DO NOT invent new emotions or adjectives.**
- **DO NOT change the User Request.** Keep the specific nouns (e.g., Rain, Winter, Drive) exactly as they are.
- Structure: "[User Request], while [Visual Context description]." or similar logical connection.
- Keep it concise and factual.
"""

    messages = [{"role": "user", "content": prompt}]
    
    payload = {
        "model": GEMMA3_MODEL, 
        "messages": messages, 
        "stream": False, 
        "options": {
            "temperature": 0.2,  # 창의성을 확 낮춰서(0.2) 있는 그대로 합치게 유도
            "num_ctx": 4096
        }
    }

    try:
        res = requests.post(f"{OLLAMA_URL}/api/chat", json=payload, timeout=120)
        res.raise_for_status()
        
        raw = res.json().get("message", {}).get("content", "").strip()
        print(f"🔍 [Debug] Raw LLM Output: {raw}") 

        # 따옴표 제거
        cleaned_sentence = raw.strip().strip('"').strip("'")
        
        if len(cleaned_sentence) < 5:
             raise ValueError("Output too short")

        return cleaned_sentence

    except Exception as e:
        print(f"⚠️ Merge failed: {e}")
        print("👉 Using simple concatenation as fallback.")
        
        # LLM 실패 시, 그냥 물리적으로 합쳐서 반환 (가장 확실한 방법)
        if text1 and text2:
            return f"{text1}. Context: {text2}"
        return text1 if text1 else text2
    
# =========================================================
# Agent 3-2: 키워드 추출 (소리 + 환경/배경 밸런스)
# =========================================================
def extract_keywords(merged_text: str, k: int = 5):
    if not merged_text.strip():
        return []

    print("💬 [Agent 3] Extracting vibe & atmospheric keywords...")

    # [수정 포인트] 'Sound Engineer' -> 'Vibe Curator'로 변경
    # 소리뿐만 아니라 계절, 시간, 장소 같은 명사도 중요하다고 명시
    system_prompt = """
You are a Content Analyst for Music Recommendation.
Your task is to identify the **Key Subject** and **Atmosphere** of the sentence.

You MUST extract keywords in this specific order of priority:
1. **The Main Subject/Setting (Nouns)**: What is the main element? (e.g., Rain, Night, Winter, Ocean, Drive, Coffee). **This is MANDATORY.**
2. **The Sound Texture (Adjectives)**: How does it sound? (e.g., Soft, Quiet, Acoustic, Jazzy).
3. **The Emotional Vibe**: (e.g., Melancholic, Cozy, Chill).

[Strict Rules]
- **If the sentence mentions a specific weather (Rain), time (Night), or place, include it as a keyword!**
- Do not just output abstract emotions.
- Return JSON only: {"keywords": ["noun1", "adj1", "adj2", ...]}
"""

    # 유저 프롬프트: 문장에서 중요한 단어를 놓치지 말라고 강조
    user_prompt = f"""
Extract {k} keywords from the sentence below. 
**Make sure to include the main noun (like 'Rain' or 'City') first.**

Sentence: "{merged_text}"
"""

    messages = [
        {"role": "system", "content": system_prompt},
        {"role": "user", "content": user_prompt}
    ]

    try:
        res = requests.post(
            f"{OLLAMA_URL}/api/chat",
            json={
                "model": GEMMA3_MODEL, 
                "messages": messages, 
                "stream": False, 
                "format": "json",
                "options": {"temperature": 0.3} # 0.4 정도로 올려서 명사와 형용사를 적절히 섞도록 유도
            },
            timeout=60
        )
        res.raise_for_status()

        raw = (
            res.json().get("message", {}).get("content")
            or res.json().get("response")
            or ""
        ).strip()

        parsed = json.loads(raw)
        kws = parsed.get("keywords", [])
        
        # 전처리: 소문자, 영문만, 길이 제한
        clean_kws = []
        for w in kws:
            w_clean = re.sub(r"[^a-zA-Z]", "", w.lower())
            if 2 <= len(w_clean) <= 20:
                clean_kws.append(w_clean)
        
        # 중복 제거
        return list(dict.fromkeys(clean_kws))[:k]

    except Exception as e:
        print(f"⚠️ Keyword extraction failed: {e}")
        # Fallback: 문장의 주요 명사와 형용사 추출
        # 4글자 이상인 단어들 중, 문장 뒤쪽에 있는 단어(보통 핵심어) 우선 추출
        words = re.findall(r'\b[a-zA-Z]{4,}\b', merged_text.lower())
        return list(set(words[-k:])) if words else ["chill"]
    

# =========================================================
# 세션 저장
# =========================================================
def save_to_session_simple(data, session_file):
    default_structure = {
        "session_start": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "input_korean": [],
        "input_image": [],
        "english_text_from_agent1": [],
        "english_caption_from_agent2": [],
        "merged_sentence": [],
        "english_keywords": [],
        "recommended_songs": []
    }

    if not os.path.exists(session_file):
        session_data = default_structure
    else:
        try:
            with open(session_file, "r", encoding="utf-8") as f:
                session_data = json.load(f)
        except:
            session_data = default_structure

    # append
    session_data["input_korean"].append(data["input"]["korean_text"])
    session_data["input_image"].append(data["input"]["image_path"])
    session_data["english_text_from_agent1"].append(data["english_text_from_agent1"])
    session_data["english_caption_from_agent2"].append(data["english_caption_from_agent2"])
    session_data["merged_sentence"].append(data["merged_sentence"])
    session_data["english_keywords"].append(data["english_keywords"])
    session_data["recommended_songs"].append(data["recommended_songs"])

    with open(session_file, "w", encoding="utf-8") as f:
        json.dump(session_data, f, ensure_ascii=False, indent=2)


# =========================================================
# 메인 파이프라인
# =========================================================
def run_agent_pipeline(korean_text="", image_path=""):
    session_file = os.path.join(SAVE_DIR, "active_session.json")
    full_history = get_full_conversation_history(session_file)

    # Agent 1
    english_text = korean_to_english(korean_text) if korean_text else ""

    # Agent 2
    english_caption = image_to_english_caption(image_path) if image_path else ""

    # Agent 3-1
    merged_sentence = rewrite_combined_sentence(english_text, english_caption, full_history)

    # Agent 3-2
    keywords = extract_keywords(merged_sentence, k=5)

    # ----------------------------------------------------
    # STEP 1: 세션에 먼저 keywords 포함하여 저장 (=딱 한 번 저장)
    # ----------------------------------------------------
    data = {
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "input": {"korean_text": korean_text, "image_path": image_path},
        "english_text_from_agent1": english_text,
        "english_caption_from_agent2": english_caption,
        "merged_sentence": merged_sentence,
        "english_keywords": keywords,
        "recommended_songs": []   # 나중에 update
    }

    save_to_session_simple(data, session_file)

    # ----------------------------------------------------
    # STEP 2: 임베딩 생성 (이미 keywords 저장된 상태)
    # ----------------------------------------------------
    from user_keyword_embedding import (
        get_active_session_id,
        get_user_keyword_embedding_active,
        get_weighted_user_embedding_active,
        save_embedding
    )

    session_id = get_active_session_id()

    latest_emb = get_user_keyword_embedding_active()
    save_embedding(latest_emb, session_id, mode="latest")

    weighted_emb = get_weighted_user_embedding_active()
    save_embedding(weighted_emb, session_id, mode="weighted")

    # ----------------------------------------------------
    # STEP 3: 코사인 추천 실행
    # ----------------------------------------------------
    recommended_songs_df = recommend(top_k=5)

    recommended_songs = recommended_songs_df[[
        "id", 
        "track_name",
        "artist_name",
        "recommend_score"
    ]].to_dict(orient="records")
    
    def make_embed_url(track_id):
        return f"https://open.spotify.com/embed/track/{track_id}"
    
    for song in recommended_songs:
        track_id = song["id"]

        # 앨범 커버
        song["album_cover_url"] = get_album_cover_url(track_id)

        # preview_url
        song["preview_url"] = get_preview_url(track_id)
        
        song["spotify_embed_url"] = make_embed_url(track_id)

    # ----------------------------------------------------
    # STEP 4: 방금 저장된 마지막 요소의 recommended_songs만 수정 (append 하지 않음)
    # ----------------------------------------------------
    with open(session_file, "r", encoding="utf-8") as f:
        session_data = json.load(f)

    last_index = len(session_data["recommended_songs"]) - 1
    session_data["recommended_songs"][last_index] = recommended_songs

    with open(session_file, "w", encoding="utf-8") as f:
        json.dump(session_data, f, ensure_ascii=False, indent=2)
        
    data["recommended_songs"] = recommended_songs # 수정


    return data


# =========================================================
# CLI (세션)
# =========================================================
from collections import OrderedDict

if __name__ == "__main__":
    print("\n🤖 Agent Pipeline (Cosine Recommender Version)")

    active_session_path = os.path.join(SAVE_DIR, "active_session.json")
    choice = input("새 대화를 시작하려면 'new' 입력 (Enter: 기존 이어서 진행): ").strip().lower()

    if choice == "new":
        if os.path.exists(active_session_path):
            with open(active_session_path, "r", encoding="utf-8") as f:
                old = json.load(f)
            old["session_end"] = datetime.now().strftime("%Y-%m-%d %H:%M:%S")

            archive_name = f"session_{old.get('session_id', uuid.uuid4().hex[:6])}.json"
            with open(os.path.join(SAVE_DIR, archive_name), "w", encoding="utf-8") as f:
                json.dump(old, f, ensure_ascii=False, indent=2)
            os.remove(active_session_path)

        # 새 세션 생성
        session_id = uuid.uuid4().hex[:6]
        new_session = OrderedDict([
            ("session_id", session_id),
            ("session_start", datetime.now().strftime("%Y-%m-%d %H:%M:%S")),
            ("input_korean", []),
            ("input_image", []),
            ("english_text_from_agent1", []),
            ("english_caption_from_agent2", []),
            ("merged_sentence", []),
            ("english_keywords", []),
            ("recommended_songs", [])
        ])
        with open(active_session_path, "w", encoding="utf-8") as f:
            json.dump(new_session, f, ensure_ascii=False, indent=2)

        print(f"🆕 새 세션 생성 완료 (ID: {session_id})")
    else:
        print("➡ 기존 세션 이어서 진행합니다.\n")

    text = input("한국어 텍스트 입력: ").strip()
    img = input("이미지 경로 입력 (Enter 허용): ").strip()

    result = run_agent_pipeline(text, img)
    print(json.dumps(result, indent=2, ensure_ascii=False))
