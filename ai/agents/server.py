# server.py
# -*- coding: utf-8 -*-
"""
Flask API for AI Recommendation (Base64 + LLaVA Image Captioning)
- Spring Boot → Flask
- 지원되는 이미지 입력:
    1) imageUrls (base64 string)
    2) data:image/png;base64,... 형태
- Base64 이미지를 /tmp 에 저장 후 Agent2에 파일 경로로 전달
"""

from flask import Flask, request, jsonify
import sys, os, json, base64, uuid
from PIL import Image
from io import BytesIO

# ===== 경로 설정 =====
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.join(os.path.dirname(__file__), "agents"))

# ===== 내부 모듈 import =====
try:
    from cosine_recommender import run_agent_pipeline
except ImportError as e:
    print("❌ cosine_recommender.py 불러오기 실패:", e)
    exit()

# ===== Flask 설정 =====
app = Flask(__name__)


# ===== Base64 → PIL Image 변환 함수 =====
def decode_base64_to_image(b64_string: str):
    try:
        # "data:image/png;base64,..." 형태일 경우 뒤쪽만 추출
        if "," in b64_string:
            b64_string = b64_string.split(",")[1]

        img_bytes = base64.b64decode(b64_string)
        img = Image.open(BytesIO(img_bytes))
        return img
    except Exception as e:
        print(f"❌ Base64 이미지 디코딩 실패: {e}")
        return None


# ===== AI 추천 API =====
@app.route("/api/recommend", methods=["POST"])
def recommend():
    try:
        data = request.get_json()
        if not data:
            return jsonify({"error": "no json body"}), 400

        session_id = data.get("sessionId", "")
        topic = data.get("topic", "")
        korean_text = data.get("inputText", "")

        # ★ 중요: 너가 실제로 보내는 필드명 = "imageUrls"
        image_base64 = data.get("imageUrls", None)

        print(f"\n🚀 [Flask] Received request (session={session_id})")
        print(f"🗣️ Text: {korean_text}")
        print(f"🖼️ Base64 image received? = {True if image_base64 else False}")

        # ===== Base64 이미지 처리 =====
        image_path = ""

        if image_base64:
            img_object = decode_base64_to_image(image_base64)

            if img_object:
                print("🖼️ Base64 이미지 디코딩 성공")

                # 파일명 충돌 방지
                tmp_path = f"/tmp/wink_img_{uuid.uuid4().hex}.png"
                img_object.save(tmp_path)
                image_path = tmp_path

                print(f"📁 저장된 이미지 경로: {image_path}")

            else:
                print("⚠️ 이미지 디코딩 실패 → 이미지 없이 진행")

        # ===== Agent Pipeline 실행 =====
        result = run_agent_pipeline(
            korean_text=korean_text,
            image_path=image_path     # ★ Agent2는 파일 경로 필요
        )

        english_keywords = result.get("english_keywords", [])
        recommended_songs = result.get("recommended_songs", [])
        merged_sentence = result.get("merged_sentence", "")

        # ===== 응답 JSON =====
        response_data = {
            "sessionId": session_id,
            "topic": topic,
            "aiMessage": f"'{topic}'에 어울리는 음악을 추천합니다.",
            "keywords": english_keywords,
            "mergedSentence": merged_sentence,
            "recommendations": [
                {
                    "songId": song.get("id") or 0,
                    "title": song.get("track_name"),
                    "artist": song.get("artist_name"),
                    "albumCover": song.get("album_cover_url") or "",
                    "previewUrl": song.get("preview_url") or "",
                }
                for song in recommended_songs
            ],
        }

        print(f"🎵 추천 완료: {[s.get('track_name') for s in recommended_songs]}")
        return jsonify(response_data), 200

    except Exception as e:
        print("🔥 서버 내부 오류:", e)
        return jsonify({"error": str(e)}), 500


# ===== 헬스 체크 =====
@app.route("/")
def home():
    return jsonify({"message": "AI Flask Server Running (Base64 Mode)"})


@app.route("/health")
def health():
    return jsonify({"status": "ok"}), 200


# ===== 서버 실행 =====
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
