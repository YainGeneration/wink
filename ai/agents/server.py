# server.py
# -*- coding: utf-8 -*-
"""
Flask API for Wink AI Full Pipeline
"""

from flask import Flask, request, jsonify
from datetime import datetime
import sys, os, json, base64, uuid, random
from PIL import Image
from io import BytesIO

# ===== 경로 설정 =====
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.join(os.path.dirname(__file__), "agents"))

# ===== main pipeline import =====
try:
    from agent3_keywordExtractor import run_agent_pipeline
except ImportError as e:
    print(f"❌ run_agent_pipeline 불러오기 실패: {e}")
    exit()

app = Flask(__name__)


# --------------------------------------------------------
# Base64 → Image 변환
# --------------------------------------------------------
def normalize_base64(raw):
    """
    imageBase64는 다음 3가지 형태 중 하나로 올 수 있다:
    1) null
    2) "xxxx" 또는 "data:image/png;base64,xxxx"
    3) ["xxxx"] 또는 ["data:image/png;base64,xxxx"]

    목적:
    무조건 Base64 문자열을 리스트 한 개로 감싸서 반환.
    """
    
    # 1) Null / 빈값
    if raw is None:
        return None

    # 2) raw가 문자열인 경우
    if isinstance(raw, str):
        stripped = raw.strip()
        if stripped == "" or stripped.lower() == "null":
            return None

        # data:image/jpeg;base64, prefix 제거
        if "," in stripped:
            stripped = stripped.split(",", 1)[1]

        return [stripped]  # 반드시 리스트로 감싸서 반환

    # 3) raw가 배열인 경우
    if isinstance(raw, list) and raw:
        first = raw[0]

        if not isinstance(first, str) or not first.strip():
            return None

        stripped = first.strip()

        if "," in stripped:
            stripped = stripped.split(",", 1)[1]

        return [stripped]

    # 그 외 형식은 무효 처리
    return None

def decode_base64_to_image(b64_str):
    """
    Base64 문자열을 PIL 이미지 객체로 변환.
    Agent1~3 이미지 캡션 단계에서 필요함.
    """
    try:
        img_bytes = base64.b64decode(b64_str)
        return Image.open(BytesIO(img_bytes))
    except Exception as e:
        print(f"decode_base64_to_image() 오류: {e}")
        return None


# --------------------------------------------------------
# AI 추천 API
# --------------------------------------------------------

@app.route("/api/recommend", methods=["POST"])
def recommend():
    image_path = ""

    try:
        data = request.get_json(silent=True)

        if not data:
            print("❌ JSON body 없음")
            return jsonify({"error": "no json body"}), 400

        session_id = data.get("sessionId")
        topic = data.get("topic", "")
        korean_text = data.get("inputText", "")

        # --- Base64 정규화 ---
        raw_image = data.get("imageBase64")
        image_base64_list = normalize_base64(raw_image)

        # --- Location / Nearby music ---
        location_data = data.get("location")
        nearby_music = data.get("nearbyMusic", [])

        print(f"\n🚀 [Flask] Received (session={session_id})")
        print(f"🗣️ inputText = {korean_text}")
        print(f"🖼️ imageBase64 exists = {bool(image_base64_list)}")
        print(f"📍 location exists = {bool(location_data)}")

        # ----------------------------------------------------
        # Agent4: 위치 + 이미지 기반
        # ----------------------------------------------------
        if location_data and image_base64_list:
            print("\n--- 🚀 Agent4 (위치 기반) 파이프라인 실행 ---")

            location_payload = {
                "imageBase64": image_base64_list,
                "location": location_data,
                "nearbyMusic": nearby_music,
            }
            result = run_agent_pipeline(location_payload=location_payload)

        # ----------------------------------------------------
        # Agent1~3: 일반 텍스트/이미지
        # ----------------------------------------------------
        else:
            if image_base64_list:
                img = decode_base64_to_image(image_base64_list[0])
                if img:
                    tmp_path = f"/tmp/wink_img_{uuid.uuid4().hex}.png"
                    img.save(tmp_path)
                    image_path = tmp_path
                    print(f"📁 Saved image at {image_path}")

            print("\n--- 🚀 Agent1~3 (일반) 파이프라인 실행 ---")
            result = run_agent_pipeline(
                korean_text=korean_text,
                image_path=image_path
            )

        # ----------------------------------------------------
        # 결과 정리
        # ----------------------------------------------------
        english_text = result.get("english_text_from_agent1", "")
        english_caption = result.get("english_caption_from_agent2", "")
        merged_sentence = result.get("merged_sentence", "")
        keywords = result.get("english_keywords", [])
        image_description_ko = result.get("korean_caption_from_agent2")
        recommended_raw = result.get("recommended_songs", [])

        JAMENDO_PREVIEW_BASE_URL = "https://storage.mp3-jamendo.com/download.php?trackid="

        recommended = []
        used_random_numbers = set()

        for song in recommended_raw:
            track_id_full = song.get("track_id", "")
            track_name = song.get("track_name")
            artist_name = song.get("artist_name")
            duration_sec = song.get("duration")
            web_url = song.get("url")

            if not track_id_full or not track_name:
                continue

            # duration 변환
            duration_ms = None
            if duration_sec:
                try:
                    duration_ms = int(float(duration_sec) * 1000)
                except:
                    pass

            # preview url
            preview_url = ""
            if track_id_full.startswith("track_"):
                try:
                    num = int(track_id_full.split("_")[1])
                    preview_url = f"{JAMENDO_PREVIEW_BASE_URL}{num}&format=mp3"
                except:
                    pass

            # 랜덤 앨범 커버
            while True:
                r = random.randint(1, 10000)
                if r not in used_random_numbers:
                    used_random_numbers.add(r)
                    break

            album_cover_url = f"https://picsum.photos/200/200?random={r}"

            recommended.append({
                "songId": track_id_full,
                "title": track_name,
                "artist": artist_name,
                "albumCover": album_cover_url,
                "previewUrl": preview_url,
                "spotifyEmbedUrl": None,
                "durationMs": duration_ms,
                "trackUrl": web_url,
            })

        response = {
            "sessionId": session_id,
            "topic": topic,
            "aiMessage": "요청하신 음악 추천 결과입니다.",
            "englishText": english_text,
            "englishCaption": english_caption,
            "imageDescriptionKo": image_description_ko,
            "mergedSentence": merged_sentence,
            "keywords": keywords,
            "recommendations": recommended,
            "timestamp": datetime.now().isoformat(),
        }

        print("\n📦 FINAL RESPONSE JSON:")
        print(json.dumps(response, indent=2, ensure_ascii=False))

        # 임시 파일 삭제
        if image_path and os.path.exists(image_path):
            os.remove(image_path)

        return jsonify(response), 200

    except Exception as e:
        print("🔥 서버 내부 오류:", e)
        if image_path and os.path.exists(image_path):
            os.remove(image_path)
        return jsonify({"error": str(e)}), 500


@app.route("/health")
def health():
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
