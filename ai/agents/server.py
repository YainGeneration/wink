# server.py
# -*- coding: utf-8 -*-
"""
Flask API for Wink AI Full Pipeline
- Spring Boot → Flask
- 입력: 한국어 텍스트 + Base64 이미지
- 처리: main pipeline (run_agent_pipeline)
- 출력: Gemma3 기반 merged sentence, keywords, song recommendations 전체 반환
"""

from flask import Flask, request, jsonify
from datetime import datetime
import sys, os, json, base64, uuid
from PIL import Image
from io import BytesIO

# ===== 경로 설정 =====
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.join(os.path.dirname(__file__), "agents"))

# ===== main pipeline import =====
try:
    from cosine_recommender import run_agent_pipeline
except ImportError as e:
    print("❌ run_agent_pipeline 불러오기 실패:", e)
    exit()

app = Flask(__name__)


# --------------------------------------------------------
# Base64 → PIL Image 변환 함수
# --------------------------------------------------------
def decode_base64_to_image(b64_string: str):
    try:
        if "," in b64_string:
            b64_string = b64_string.split(",")[1]

        img_bytes = base64.b64decode(b64_string)
        img = Image.open(BytesIO(img_bytes))
        return img

    except Exception as e:
        print(f"❌ Base64 디코딩 실패: {e}")
        return None


# --------------------------------------------------------
# AI 추천 API
# --------------------------------------------------------
@app.route("/api/recommend", methods=["POST"])
def recommend():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "no json body"}), 400

        session_id = data.get("sessionId", "")
        topic = data.get("topic", "")
        korean_text = data.get("inputText", "")
        image_base64 = data.get("imageBase64", None)

        print(f"\n🚀 [Flask] Received (session={session_id})")
        print(f"🗣️ inputText: {korean_text}")
        print(f"🖼️ Base64 exists? {bool(image_base64)}")

        # -------------------------------------------
        # Base64 이미지 디코딩 -> /tmp 저장
        # -------------------------------------------
        image_path = ""
        if image_base64:
            img_object = decode_base64_to_image(image_base64)
            if img_object:
                tmp_path = f"/tmp/wink_img_{uuid.uuid4().hex}.png"
                img_object.save(tmp_path)
                image_path = tmp_path
                print(f"📁 Saved image: {image_path}")

        # -------------------------------------------
        # 실행: Full main pipeline (Agent1~Agent3 + RAG)
        # -------------------------------------------
        result = run_agent_pipeline(
            korean_text=korean_text,
            image_path=image_path
        )

        # -------------------------------------------
        # 결과 구성
        # -------------------------------------------
        english_text = result.get("english_text_from_agent1")
        english_caption = result.get("english_caption_from_agent2")
        merged_sentence = result.get("merged_sentence")
        keywords = result.get("english_keywords", [])
        recommended_raw = result.get("recommended_songs", [])

        # 🔥 Spring DTO 구조에 맞게 변환
        recommended = []
        for song in recommended_raw:
            recommended.append({
                "songId": song.get("id"),
                "title": song.get("track_name"),
                "artist": song.get("artist_name"),
                "albumCover": song.get("album_cover_url"),
                "previewUrl": song.get("preview_url"),
                "spotify_embed_url": song.get("spotify_embed_url")
            })

        response_data = {
            "sessionId": session_id,
            "title": topic,
            "aiMessage": f"'요청하신 음악 추천 결과입니다.",
            "english_text": english_text,
            "english_caption": english_caption,
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
        return jsonify({"error": str(e)}), 500


# --------------------------------------------------------
# Health check
# --------------------------------------------------------
@app.route("/health")
def health():
    return jsonify({"status": "ok"}), 200


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
