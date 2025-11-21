# main pipeline
# -*- coding: utf-8 -*-
"""
Agent3 (통합 파이프라인)
- Agent 1 로직: 한국어 텍스트 입력 → 영어 번역 (EXAONE)
- Agent 2 로직: 이미지 경로 입력 → 영어 캡션 (Ollama Gemma3)
- Agent 3 로직 (1): 두 영어 문장 → 하나의 문장으로 재작성 (Ollama Gemma3)
- Agent 3 로직 (2): 재작성된 문장 → 영어 키워드 5개 추출 (Ollama Gemma3)
- 세션 관리: 모든 결과를 'active_session.json'에 누적 저장
- Agent 4 로직: 위치 + 이미지 + 주변 음악 기반 추천 파이프라인 (recommend_with_image_and_nearby_users)
"""

import os
import re
import json
from datetime import datetime
import requests
import uuid
from collections import OrderedDict # CLI 로직에서 필요

# agent1 import
try:
    from agent1_exaone import korean_to_english
except ImportError:
    print("❌ 'agents/agent1_exaone.py' 파일을 찾을 수 없습니다.")
    exit()

# agent2 import
try:
    from agent2_imageToEng import image_to_english_caption, caption_from_base64, enhance_caption_with_location
except ImportError:
    print("❌ 'agents/agent2_imageToEng.py' 파일을 찾을 수 없습니다.")
    exit()
    
# rag import
try:
    from context_manager import get_full_conversation_history
except ImportError:
    print("❌ 'agents/context_manager.py' 파일을 찾을 수 없습니다.")
    exit()
    
# rag retriever - song recommendation import
try:
    from rag_retriever import get_song_recommendations, get_vector_db, embed_text
except ImportError:
    print("❌ 'rag/rag_retriever.py' 파일을 찾을 수 없습니다.")
    exit()
        
# =========================================================
# 1. 전역 설정
# =========================================================
OLLAMA_URL = "http://localhost:11434"
GEMMA3_MODEL = "gemma3:4b"
SAVE_DIR = "agents/keywords"
os.makedirs(SAVE_DIR, exist_ok=True)

# =========================================================
# 3. [Agent 3-1] 두 영어 문장 합치기 : 모델 안쓰고 그냥 문장 합칠지 고민중
# =========================================================
def rewrite_combined_sentence(text1: str, text2: str, full_history: str) -> str:    
    new_input_sentence = f"{text1} {text2}".strip()
    if not new_input_sentence:
        # (예: "비 오는 날" -> "더 차분하게")
        # 새 입력(text1, text2)이 없더라도, 이전 이력(full_history)만으로
        # Gemma3가 키워드를 다시 생성하도록 유도할 수 있습니다.
        # 하지만 여기서는 새 입력이 없으면 에러로 간주하고 빈 문자열 반환
        print("⚠️ [Agent 3] No new input text or image provided.")
        return ""

    print("🧩 [Agent 3] Merging (Context + New Input) sentences (Ollama)...")
    # [핵심] 👈 Gemma3에게 '이전 대화'와 '새 요청'을 함께 전달
    prompt = f"""
You are a context-aware chat assistant. Your job is to understand the user's full request by combining their past conversation history with their newest input.

[Past Conversation History]
{full_history}

[User's Newest Input]
"{new_input_sentence}"

Task:
Create ONE final descriptive sentence that reflects only the user's latest intention.

Rules:
1. Past history is for reference.
2. The newest input overrides or replaces previous intent if different.
3. Do NOT preserve previous meanings when the new input changes the mood/direction.
4. Focus on the newest input as the dominant signal.

Respond *only* with the final combined English sentence.
"""
    
    messages = [{"role": "user", "content": prompt.strip()}]
    payload = {"model": GEMMA3_MODEL, "messages": messages, "stream": False}
    try:
        res = requests.post(f"{OLLAMA_URL}/api/chat", json=payload, timeout=60)
        res.raise_for_status()
        
        raw_response = (res.json().get("message", {}).get("content", "") or "").strip()
        match = re.search(r'["\'](.*?_*)["\']', raw_response)
        if match:
            return match.group(1).strip()
        return raw_response.split('\n')[-1].strip()
        
    except Exception as e:
        print(f"⚠️ Merge failed: {e}")
        return new_input_sentence # 실패 시 새 입력만 반환
    
def extract_keywords(merged_text: str, full_history: str, k: int = 5) -> list[str]:
    if not merged_text.strip():
        return []

    system_prompt = f"""
You are a Music Context Understanding & Keyword Extraction Expert.
Extract EXACTLY {k} keywords that best represent the user's musical intent.

### STRICT RULES ###

1. **Primary Subject / Setting (NOUNS)** - If the sentence includes a main noun (night, rain, drive, study, winter, ocean, city), 
     include EXACTLY ONE such noun as the FIRST keyword.
   - Do NOT stop at only one keyword. It only defines the *first* slot.

2. **Sound Texture (Adjective or Style Words)** - Fill at least 1–2 of the remaining keywords with sound-related adjectives  
     (soft, acoustic, ambient, mellow, electronic, jazzy, gentle).

3. **Emotional Vibe (Feels / Mood)** - Include at least 1 emotional keyword  
     (calm, sweet, dreamy, nostalgic, romantic, angry, peaceful).

4. **User Expression Preservation (Non-musical expressions allowed)** - If the user expresses feelings like “달달한”, “짜증나는”, “따뜻한”,  
     you MUST include the English equivalent in the final keywords  
     (sweet, irritated, warm, refreshing).

5. **ABSOLUTE RULE** - You MUST output **exactly {k} keywords**, no fewer.  
   - If fewer than {k} suitable terms exist, expand using closely-related semantic descriptors.  
   - NEVER output only one keyword.

Output ONLY valid JSON:
{{"keywords": ["k1", "k2", "k3", "k4", "k5"]}}
"""

    user_prompt = f"""
Past conversation:
{full_history}

User's latest intent:
"{merged_text}"

Extract the final {k} refined keywords following all rules above.
"""

    payload = {
        "model": GEMMA3_MODEL,
        "messages": [
            {"role": "system", "content": system_prompt},
            {"role": "user", "content": user_prompt}
        ],
        "stream": False,
        "format": "json"
    }

    try:
        res = requests.post(f"{OLLAMA_URL}/api/chat", json=payload, timeout=60)
        res.raise_for_status()

        raw_output = (
            res.json().get("message", {}).get("content")
            or res.json().get("response")
            or ""
        ).strip()

        parsed = json.loads(raw_output)
        kws = parsed.get("keywords", [])

        # Clean
        clean = []
        for w in kws:
            w = re.sub(r"[^a-zA-Z]", "", w.lower())
            if 2 <= len(w) <= 20:
                clean.append(w)

        # Ensure exactly k
        return list(dict.fromkeys(clean))[:k]

    except Exception as e:
        print(f"🔥 Keyword extraction failed: {e}")
        return ["night", "calm", "soft", "sweet", "ambient"][:k]

    
# =========================================================
# 8. 세션 저장 - 나의 순간
# =========================================================
def save_to_session_simple(data: dict, session_file: str):
    """
    지정된 세션 JSON 파일을 안전하게 열고, 데이터를 append합니다.
    파일이 없으면 새로 생성합니다.
    """
    default_structure = {
        "session_start": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "input_korean": [], "input_image": [],
        "english_text_from_agent1": [], "english_caption_from_agent2": [],
        "merged_sentence": [], "english_keywords": [],
        "recommended_songs": []
    }
    
    if os.path.exists(session_file):
        try:
            with open(session_file, "r", encoding="utf-8") as f:
                session_data = json.load(f)
            for key, default_value in default_structure.items():
                if key not in session_data:
                    session_data[key] = default_value
        except json.JSONDecodeError:
            print(f"⚠️ 세션 파일이 손상되어 새로 만듭니다: {session_file}")
            session_data = default_structure
    else:
        session_data = default_structure

    try:
        session_data["input_korean"].append(data["input"]["korean_text"])
        session_data["input_image"].append(data["input"]["image_path"])
        session_data["english_text_from_agent1"].append(data["english_text_from_agent1"])
        session_data["english_caption_from_agent2"].append(data["english_caption_from_agent2"])
        session_data["merged_sentence"].append(data["merged_sentence"])
        session_data["english_keywords"].append(data["english_keywords"])
        session_data["recommended_songs"].append(data["recommended_songs"])
        
        # ➕ 추가: track_id 누적 저장
        if "recommended_track_ids" not in session_data:
            session_data["recommended_track_ids"] = []

        for song in data["recommended_songs"]:
            tid = song.get("track_id")
            if tid and tid not in session_data["recommended_track_ids"]:
                session_data["recommended_track_ids"].append(tid)
                
    except KeyError as e:
        print(f"🔥 데이터 저장 중 치명적인 Key Error 발생: {e}")
        return

    with open(session_file, "w", encoding="utf-8") as f:
        json.dump(session_data, f, ensure_ascii=False, indent=2)
        
# =========================================================
# 세션 저장 - 나의 공간
def save_location_recommend_full(data: dict):
    """
    Agent4 추천 결과를 전체 세션 형식으로 지정된 경로에 저장합니다.
    저장 경로: ai/agents/location_recommends/
    """
    
    # 1. 저장 디렉토리 설정 및 생성
    # SAVE_DIR = "agents/keywords"를 기준으로 하여, location_recommends 폴더를 생성합니다.
    location_save_dir = os.path.join("agents", "location_recommends")
    os.makedirs(location_save_dir, exist_ok=True)
    
    # 2. 파일명 생성 (나의 순간과 동일한 형식)
    random_id=str(uuid.uuid4())[:8]
    file_name = f"location_recommend_{random_id}.json"
    save_path = os.path.join(location_save_dir, file_name)
    
    input_data = data.get("input", {}) # 'input' 키가 없을 경우 대비

    # 3. JSON 저장
    with open(save_path, "w", encoding="utf-8") as f:
        # Agent4 결과는 단일 호출이므로 리스트가 아닌 단일 딕셔너리 형태로 저장합니다.
        # 세션 구조와 형식은 같되, 리스트 형태를 단일 값으로 변환하여 저장 (선택적)
        output_data = {
            "timestamp": data.get("timestamp"),
            "input_location": input_data.get("location"),
            "input_image": input_data.get("image_path", ""),
            "english_caption_from_agent2": data.get("english_caption_from_agent2"),
            "english_keywords": data.get("english_keywords"),
            "recommended_songs": data.get("recommended_songs"),
        }
        json.dump(output_data, f, ensure_ascii=False, indent=2)

    print(f"💾 Saved location recommend result → {save_path}")
    return save_path
        
# -------------------------------------------------------
# 주변 사람이 듣는 노래를 RAG DB에서 매칭
# -------------------------------------------------------
def match_song_in_rag(title: str, artist: str, top_k=1):
    """
    주변 사람이 듣는 노래(title, artist)를 하나의 문장으로 묶어서
    RAG DB에서 가장 유사한 Jamendo 노래를 검색한다.
    """
    db = get_vector_db()  # Chroma DB
    query = f"{title} {artist}"

    query_vec = embed_text(query)

    results = db.similarity_search_with_score(query, k=top_k)

    matched = []
    for r, score in results:
        meta = r.metadata
        meta["similarity_score"] = score
        matched.append(meta)

    return matched


# -------------------------------------------------------
# 주변 음악 기반으로 유사 노래 찾기
# -------------------------------------------------------
def recommend_from_nearby_music(nearbyMusic: list):
    """
    각 주변 음악을 RAG DB에서 매칭 → 유사한 노래 추천
    """
    all_recs = []

    for m in nearbyMusic:
        # **[수정]** CLI와 API의 키를 통일하여 'title', 'artist' 사용
        title = m.get("title", "") 
        artist = m.get("artist", "")

        # songTitle, artistName으로 들어올 경우 (이전 CLI 코드와의 호환성을 위해 유지)
        if not title:
             title = m.get("songTitle", "") 
        if not artist:
            artist = m.get("artistName", "")

        if not title and not artist:
            continue
            
        # 1) RAG DB에서 K-pop → Jamendo 곡 매칭
        matched = match_song_in_rag(title, artist, top_k=1)
        if not matched:
            continue

        anchor_song = matched[0]
        print(f"🎧 Anchor Matched → {anchor_song['track_name']} / {anchor_song['artist_name']}")

        # 2) 해당 Jamendo 곡과 유사한 음악 추가 추천
        anchor_keywords = [
            anchor_song.get("genre_tags", ""),
            anchor_song.get("mood_tags", ""),
            anchor_song.get("track_name", "")
        ]
        anchor_keywords = " ".join(anchor_keywords)

        recs = get_song_recommendations(anchor_keywords.split(), top_k=2)
        all_recs.extend(recs)

    # 중복 제거
    seen = set()
    unique = []
    for r in all_recs:
        tid = r["track_id"]
        if tid not in seen:
            seen.add(tid)
            unique.append(r)

    return unique


# # -------------------------------------------------------
# # 메인 추천: 이미지 + 주변 음악
# # -------------------------------------------------------
# def recommend_with_image_and_nearby_users(image_b64: str,
#                                           place_name: str,
#                                           nearbyMusic: list):

#     # 1) 이미지 → 캡션
#     caption = caption_from_base64(image_b64)
#     print("📷 Caption:", caption)

#     # 2) 장소 기반 보정
#     enhanced_caption = enhance_caption_with_location(caption, place_name)
#     print("📍 Enhanced Caption:", enhanced_caption)

#     # 3) 이미지 기반 키워드 추출
#     user_keywords = extract_keywords(
#         merged_text=enhanced_caption,
#         full_history="",
#         k=5
#     )
#     print("🎨 Image Keywords:", user_keywords)

#     # 4) 이미지 기반 추천
#     img_recs = get_song_recommendations(user_keywords, top_k=2)

#     # 5) 주변 음악 기반 추천
#     near_recs = recommend_from_nearby_music(nearbyMusic)

#     # 6) 두 추천 리스트 합쳐서 최종 3곡만
#     combined = img_recs + near_recs

#     # 중복 제거
#     seen = set()
#     final = []
#     for r in combined:
#         tid = r["track_id"]
#         if tid not in seen:
#             seen.add(tid)
#             final.append(r)
#         if len(final) >= 1:
#             break

#     return {
#         "caption": enhanced_caption,
#         "keywords": user_keywords,
#         "recommended_songs": final
#     }

# 저장 코드
def save_location_recommend(result: dict):
    """
    Agent4 추천 결과를 JSON 파일로 저장. (사용되지 않는 레거시 함수일 수 있음)
    저장 파일명: location_recommend_YYYYmmdd_HHMMSS.json
    """
    save_path = os.path.join(
        SAVE_DIR,
        f"location_{datetime.now().strftime('%Y%m%d_%H%M%S')}.json"
    )

    # recommended_songs는 1곡만 온다는 전제
    if not result.get("recommended_songs"):
        print("❌ No recommended songs to save.")
        return None

    # **[수정]** recommended_songs의 모든 곡을 저장하도록 변경 (기존: 1곡만 저장)
    output_json = {
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "recommendations": []
    }
    
    for song in result["recommended_songs"]:
        output_json["recommendations"].append({
            "songId": song.get("track_id"),
            "title": song.get("track_name"),
            "artist": song.get("artist_name"),
            "durationMs": int(song.get("duration", 0) * 1000),  # 초 → ms 변환
            "trackUrl": song.get("url")
        })


    with open(save_path, "w", encoding="utf-8") as f:
        json.dump(output_json, f, ensure_ascii=False, indent=2)

    print(f"💾 Saved recommend result → {save_path}")
    return save_path


# nearby_users.json 읽어오기
def load_nearby_users_json():
    path = "agents/nearby_users.json"
    if not os.path.exists(path):
        print("❌ nearby_users.json 파일이 존재하지 않습니다.")
        return None

    with open(path, "r", encoding="utf-8") as f:
        return json.load(f)
    
    
def run_location_recommendation():
    """
    nearby_users.json 을 읽어서:
    - imagePath 기반 이미지 캡션 생성
    - 주변 사용자 노래 기반 추천
    """
    payload = load_nearby_users_json()
    if payload is None:
        raise ValueError("nearby_users.json 로딩 실패")

    image_path = payload.get("imagePath")
    nearbyMusic = payload.get("nearbyMusic", [])

    if not image_path:
        raise ValueError("❌ nearby_users.json 에 'imagePath'가 없습니다.")

    # 1) 이미지 → 캡션
    caption = image_to_english_caption(image_path)

    # 장소 보정 없음
    enhanced_caption = caption

    # 2) 키워드 추출
    keywords = extract_keywords(enhanced_caption, full_history="", k=5)

    # 3) 이미지 기반 추천
    img_recs = get_song_recommendations(keywords, top_k=3)

    # 4) 주변 사용자 기반 추천
    near_recs = recommend_from_nearby_music(nearbyMusic)

    # 5) 최종 1곡 선택
    combined = img_recs + near_recs
    final = []
    seen = set()

    for r in combined:
        tid = r["track_id"]
        if tid not in seen:
            seen.add(tid)
            final.append(r)
        if len(final) == 1:
            break

    return {
        "caption": enhanced_caption,
        "keywords": keywords,
        "nearby_users": nearbyMusic,
        "image_path": image_path,
        "recommended_songs": final
    }


# =========================================================
# 9. 메인 파이프라인
# =========================================================
def run_agent_pipeline(korean_text="", image_path="", location_payload=None) -> dict:    
    
    # 1) 위치 기반 분석 요청이면, Agent4 실행
    if location_payload:
        print("📍 Running Location-Based Recommendation (Agent4 Mode)")
        result = run_location_recommendation()
        
        # 저장 구조는 기존처럼 유지
        data = {
            "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "input": {
                "korean_text": "이미지 + 주변 사용자 기반 추천 요청",
                "image_path": result["image_path"]
            },
            "english_caption_from_agent2": result["caption"],
            "english_keywords": result["keywords"],
            "recommended_songs": result["recommended_songs"]
        }

        save_location_recommend_full(data)
        return data
    
    # ----------- 일반 텍스트/이미지 기반 추천 흐름 -----------

    # 세션 파일 경로
    session_file_path = os.path.join(SAVE_DIR, "active_session.json")

    # 대화 이력 불러오기 (RAG용)
    full_history = get_full_conversation_history(session_file_path)

    # Agent1: 한국어 → 영어 번역
    english_text = korean_to_english(korean_text) if korean_text else ""

    # Agent2: 이미지 → 영어 캡션
    english_caption = image_to_english_caption(image_path) if image_path else ""

    # Agent3-1: 문장 합치기
    merged = rewrite_combined_sentence(english_text, english_caption, full_history)

    # Agent3-2: 키워드 추출
    eng_keywords = extract_keywords(merged, full_history)

    # 🎵 노래 추천: 초기에 넉넉하게 가져오기 (중복 제거 대비)
    recommended_songs = get_song_recommendations(eng_keywords, top_k=15)

    # --------------- 📌 필터링 로직 시작 ---------------

    # 1) Fly 포함된 앨범 제거
    recommended_songs = [
        s for s in recommended_songs
        if "fly" not in s.get("album_name", "").lower()
    ]

    # 2) 이미 추천된 곡 제거
    try:
        with open(session_file_path, "r", encoding="utf-8") as f:
            session_data = json.load(f)
        already = set(session_data.get("recommended_track_ids", []))
    except FileNotFoundError:
        already = set()

    recommended_songs = [
        s for s in recommended_songs
        if s.get("track_id") not in already
    ]

    # 3) fallback: 필터링으로 너무 줄어든 경우 다시 찾기
    if len(recommended_songs) < 3:
        fallback = get_song_recommendations(eng_keywords, top_k=40)
        fallback = [
            s for s in fallback
            if "fly" not in s.get("album_name", "").lower()
            and s.get("track_id") not in already
        ]
        recommended_songs = fallback[:3]

    else:
        recommended_songs = recommended_songs[:3]

    # --------------- 📌 필터링 로직 끝 ---------------

    # 최종 데이터 패키징
    data = {
        "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
        "input": {"korean_text": korean_text, "image_path": image_path},
        "english_text_from_agent1": english_text,
        "english_caption_from_agent2": english_caption,
        "merged_sentence": merged,
        "english_keywords": eng_keywords,
        "recommended_songs": recommended_songs,
    }

    # 세션에 저장
    save_to_session_simple(data, session_file_path)
    print(f"\n✅ Saved to active session → {session_file_path}")

    return data

# =========================================================
# 7️⃣ CLI (세션 관리자)
# =========================================================
# base64는 파일 상단에 이미 import 되어 있으므로 생략합니다.

if __name__ == "__main__":
    print("\n🤖 Agent Pipeline (세션형 실행)")

    active_session_path = os.path.join(SAVE_DIR, "active_session.json")
    # 세션 시작/이어하기 질문은 유지
    choice = input("\n새 대화를 시작하려면 'new' 입력 (기존 이어하기는 Enter): ").strip().lower()

    # ... (기존 세션 아카이빙 및 새 세션 시작 로직은 동일)
    if choice == "new":
        # 1) 기존 active_session.json 백업
        if os.path.exists(active_session_path):
            try:
                with open(active_session_path, "r", encoding="utf-8") as f:
                    old_data = json.load(f)
                end_time = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
                old_data["session_end"] = end_time

                archive_name = f"session_{uuid.uuid4().hex[:6]}.json"
                archive_path = os.path.join(SAVE_DIR, archive_name)

                with open(archive_path, "w", encoding="utf-8") as f:
                    json.dump(old_data, f, ensure_ascii=False, indent=2)

                print(f"🗂️ 세션 보관 완료: {archive_name} (session_end: {end_time})")

            except Exception as e:
                print(f"⚠️ 세션 아카이빙 오류: {e}")

            # 2) 🔥 이 줄이 새로운 세션이 정상 생성되게 하는 핵심!
            os.remove(active_session_path)

        # 3) 새로운 세션 파일 생성
        new_session = {
            "session_id": uuid.uuid4().hex[:6],
            "session_start": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
            "input_korean": [],
            "input_image": [],
            "english_text_from_agent1": [],
            "english_caption_from_agent2": [],
            "merged_sentence": [],
            "english_keywords": [],
            "recommended_songs": []
        }

        with open(active_session_path, "w", encoding="utf-8") as f:
            json.dump(new_session, f, ensure_ascii=False, indent=2)

        print(f"🆕 새 세션 생성 완료!")


    # ------------------------------------------------
    # ✨ 모드 선택 로직 추가
    # ------------------------------------------------
    print("\n--- 🌟 실행 모드 선택 ---")
    mode = input("1. 일반 텍스트/이미지 입력 (세션 기반)\n2. 위치 기반 추천 (Agent4)\n선택 (1 또는 2): ").strip()
    
    if mode == "2":
        print("\n--- 📍 위치 기반 추천 (Agent4) 실행 ---")

        try:
            with open("agents/nearby_users.json", "r", encoding="utf-8") as f:
                payload = json.load(f)

            image_path = payload.get("imagePath")
            nearbyMusic = payload.get("nearbyMusic", [])

        except Exception as e:
            print(f"❌ nearby_users.json 읽기 오류: {e}")
            exit()

        print("📄 nearby_users.json 로드 완료")
        print(f" - 이미지 경로: {image_path}")
        print(f" - 주변 사용자 음악 개수: {len(nearbyMusic)}")

        try:
            print("\n--- 🚀 Agent4 파이프라인 실행 ---")

            caption = image_to_english_caption(image_path)
            keywords = extract_keywords(caption, full_history="", k=5)

            img_recs = get_song_recommendations(keywords, top_k=3)
            near_recs = recommend_from_nearby_music(nearbyMusic)

            combined = img_recs + near_recs
            final = []
            seen = set()

            for r in combined:
                tid = r["track_id"]
                if tid not in seen:
                    seen.add(tid)
                    final.append(r)
                if len(final) == 1:
                    break

            result = {
                "timestamp": datetime.now().strftime("%Y-%m-%d %H:%M:%S"),
                "input": {
                    "korean_text": "nearby_users.json 기반 위치 추천",
                    "image_path": image_path
                },
                "english_caption_from_agent2": caption,
                "english_keywords": keywords,
                "recommended_songs": final
            }

            save_location_recommend_full(result)

            print("\n--- 🎯 실행 결과 (Agent4) ---")
            print(json.dumps(result, ensure_ascii=False, indent=2))

        except Exception as e:
            print(f"🔥 Agent4 실행 중 오류 발생: {e}")

            
    else: 
        # 1번 또는 잘못된 입력 (기본값: 일반 텍스트/이미지 모드)
        print("\n--- 💬 일반 텍스트/이미지 입력 (세션 기반) ---")
        text = input("한국어 텍스트 입력 (없으면 Enter): ").strip()
        img = input("이미지 경로 입력 (없으면 Enter): ").strip()

        if not text and not img:
            print("\n🛑 입력이 없어 종료합니다.")
            exit()

        print("\n--- 🚀 파이프라인 실행 ---")
        try:
            # run_agent_pipeline에 텍스트/이미지 경로 전달
            result = run_agent_pipeline(korean_text=text, image_path=img)
            print("\n--- 🎯 실행 결과 ---")
            print(json.dumps(result, ensure_ascii=False, indent=2))
        except Exception as e:
            print(f"\n🔥 오류 발생: {e}")