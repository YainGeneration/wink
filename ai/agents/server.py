# server.py
# -*- coding: utf-8 -*-
"""
Flask API for AI Recommendation (Base64 전용)
- Spring Boot → Flask
- Base64 이미지 + 텍스트 입력
- URL 기반 업로드/정적 파일 기능은 삭제됨
"""

from flask import Flask, request, jsonify
import sys, os, json, base64
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
def decode_base64_to_image(base64_str):
    try:
        img_bytes = base64.b64decode(base64_str)
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

        # 요청 파라미터 추출
        session_id = data.get("sessionId", "")
        topic = data.get("topic", "")
        korean_text = data.get("inputText", "")
        image_base64 = data.get("imageBase64", None)

        print(f"\n🚀 [Flask] Received request (session={session_id})")
        print(f"🗣️ Text: {korean_text}")

        # === Base64 이미지 디코딩 ===
        img_object = None
        if image_base64:
            img_object = decode_base64_to_image(image_base64)
            if img_object:
                print("🖼️ Base64 이미지 디코딩 성공")
            else:
                print("⚠️ Base64 이미지 디코딩 실패 → 이미지 없이 진행")

        # ===== Agent 파이프라인 실행 =====
        result = run_agent_pipeline(
            korean_text=korean_text,
            image=img_object  # 이미지 객체 전달
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
                    "albumCover": song.get("album_cover") or "",
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


# ===== 이미지 파일 → Base64 변환 API =====
@app.route("/api/convert-base64", methods=["POST"])
def convert_base64():
    """
    로컬 이미지 파일을 업로드하면 Base64로 변환하여 반환하는 API
    (Postman 테스트 전용)
    """
    if "file" not in request.files:
        return jsonify({"error": "file field missing"}), 400

    file = request.files["file"]

    if file.filename == "":
        return jsonify({"error": "no selected file"}), 400

    try:
        # 파일 내용을 읽어서 Base64로 변환
        file_bytes = file.read()
        base64_str = base64.b64encode(file_bytes).decode("utf-8")

        print("📸 Base64 변환 성공")
        return jsonify({
            "filename": file.filename,
            "base64": base64_str
        }), 200

    except Exception as e:
        return jsonify({"error": str(e)}), 500


# ===== 헬스체크 =====
@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "AI Flask Server Running (Base64 Mode)"})


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200


# ===== Flask 실행 =====
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
