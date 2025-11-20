# -*- coding: utf-8 -*-
"""
Flask API for Wink AI Full Pipeline (최종 수정 버전)
- run_agent_pipeline 임포트 경로 수정
- 추천곡 매핑 및 URL 필드 (trackUrl 포함) 추가 완료
"""

from flask import Flask, request, jsonify
from datetime import datetime
import sys, os, json, base64, uuid, random
from PIL import Image
from io import BytesIO

# ===== 경로 설정 =====
# Python이 'agents' 폴더 내부 모듈을 찾을 수 있도록 경로를 설정합니다.
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.join(os.path.dirname(__file__), "agents"))

# ===== main pipeline import =====
# 🚨 핵심 수정: 현재 잘 작동하는 'agent3_pipeline'에서 run_agent_pipeline을 임포트합니다.
# (실제 파일명이 'agent3_pipeline.py'가 아닐 경우 수정해야 합니다.)
try:
    from agent3_keywordExtractor import run_agent_pipeline 
except ImportError as e:
    print(f"❌ run_agent_pipeline 불러오기 실패: {e}. 'agent3_pipeline.py' 파일 이름을 확인하세요.")
    exit()

app = Flask(__name__)


# --------------------------------------------------------
# Base64 → image 변환
# --------------------------------------------------------
def decode_base64_to_image(b64_string: str):
    try:
        if "," in b64_string:
            b64_string = b64_string.split(",")[1]

        img_bytes = base64.b64decode(b64_string)
        return Image.open(BytesIO(img_bytes))

    except Exception as e:
        print(f"❌ Base64 디코딩 실패: {e}")
        return None


# --------------------------------------------------------
# AI 추천 API
# --------------------------------------------------------
@app.route("/api/recommend", methods=["POST"])
def recommend():
    # 임시 이미지 경로 초기화 (오류 발생 시 삭제를 위해)
    image_path = ""
    
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "no json body"}), 400

        session_id = data.get("sessionId", "")
        topic = data.get("topic", "")
        korean_text = data.get("inputText", "")
        image_base64 = data.get("imageBase64")

        print(f"\n🚀 [Flask] Received (session={session_id})")
        print(f"🗣️ inputText = {korean_text}")
        print(f"🖼️ imageBase64 exists = {bool(image_base64)}")

        # -------------------------------------------
        # 이미지 Base64 → /tmp 저장
        # -------------------------------------------
        if image_base64:
            img = decode_base64_to_image(image_base64)
            if img:
                tmp_path = f"/tmp/wink_img_{uuid.uuid4().hex}.png"
                img.save(tmp_path)
                image_path = tmp_path
                print(f"📁 Saved image at {image_path}")

        # -------------------------------------------
        # 실행: Agent1~3 + RAG + 추천
        # -------------------------------------------
        result = run_agent_pipeline(
            korean_text=korean_text,
            image_path=image_path
        )

        # -------------------------------------------
        # 결과 가져오기
        # -------------------------------------------
        english_text = result.get("english_text_from_agent1")
        english_caption = result.get("english_caption_from_agent2")
        merged_sentence = result.get("merged_sentence")
        keywords = result.get("english_keywords", [])

        # ❗ Agent2 한국어 설명(Gemini)은 아직 없음 → None
        image_description_ko = result.get("korean_caption_from_agent2")

        # RAG 검색 결과 (빈 리스트일 수 있음)
        recommended_raw = result.get("recommended_songs", [])
        
        # -------------------------------------------
        # 추천곡 변환 및 URL 구성
        # -------------------------------------------
        
        # 실제 Jamendo MP3 파일의 프리뷰 URL을 구성하기 위한 기본 주소 
        JAMENDO_PREVIEW_BASE_URL = "https://storage.mp3-jamendo.com/download.php?trackid="

        recommended = []
        used_random_numbers = set()

        for song in recommended_raw:
            
            # -------------------------------------------
            # RAG 결과에서 필수 데이터 추출
            # -------------------------------------------
            track_id_full = song.get("track_id", "")
            track_name = song.get("track_name")
            artist_name = song.get("artist_name")
            duration_sec = song.get("duration") # 초(second) 단위로 가정
            track_web_url = song.get("url")     # ⬅️ 웹사이트 URL 추출
            
            # RAG 결과에 필수 값이 없으면 건너뜁니다.
            if not track_id_full or not track_name:
                print(f"⚠️ Warning: RAG result missing track ID or name. Skipping song.")
                continue


            # 1. Jamendo duration(초) → ms 변환
            duration_ms = None
            if duration_sec is not None:
                try:
                    duration_ms = int(float(duration_sec) * 1000)
                except:
                    duration_ms = None
            
            # 2. MP3 Preview URL 구성
            preview_url = ""
            if track_id_full.startswith("track_"):
                track_number_str = track_id_full.split("_")[-1]
                try:
                    track_number = int(track_number_str)
                    preview_url = f"{JAMENDO_PREVIEW_BASE_URL}{track_number}&format=mp3"
                except ValueError:
                    print(f"⚠️ Warning: Could not parse track number from ID: {track_id_full}")
                    pass 
            
            # 3. Album Cover (Picsum 플레이스홀더)
            rand_num = None
            while True:
                candidate = random.randint(1, 10000)
                if candidate not in used_random_numbers:
                    used_random_numbers.add(candidate)
                    rand_num = candidate
                    break
            album_cover_url = f"https://picsum.photos/200/200?random={rand_num}"


            # 4. 최종 응답 구조에 맞게 매핑
            recommended.append({
                "songId": track_id_full,                         
                "title": track_name,                             
                "artist": artist_name,                           
                "albumCover": album_cover_url,
                "previewUrl": preview_url,                       
                "spotify_embed_url": None,                
                "durationMs": duration_ms,
                "trackUrl": track_web_url  # ⬅️ 웹사이트 URL 필드 추가
            })

        # -------------------------------------------
        # 최종 응답 구성
        # -------------------------------------------
        response_data = {
            "sessionId": session_id,
            "title": topic,
            "aiMessage": "요청하신 음악 추천 결과입니다.",
            "english_text": english_text,
            "english_caption": english_caption,
            "imageDescriptionKo": image_description_ko,
            "mergedSentence": merged_sentence,
            "keywords": keywords,
            "recommendations": recommended,
            "timestamp": datetime.now().isoformat()
        }

        print("\n📦 FINAL RESPONSE JSON:")
        print(json.dumps(response_data, indent=2, ensure_ascii=False))

        return jsonify(response_data), 200

    except Exception as e:
        print("🔥 서버 내부 오류:", e)
        # 모든 임시 파일 삭제 (예방적 조치)
        if image_path and os.path.exists(image_path):
            os.remove(image_path)
            print(f"🗑️ Deleted temp image: {image_path}")
            
        return jsonify({"error": str(e)}), 500


# --------------------------------------------------------
@app.route("/health")
def health():
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)