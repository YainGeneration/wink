# server.py
# -*- coding: utf-8 -*-
"""
Flask API for AI Recommendation
- Agent3 통합 파이프라인(run_agent_pipeline) 직접 호출
- Spring Boot (http://localhost:8080) → Flask (http://127.0.0.1:5001) 연결용
- 이미지 업로드 엔드포인트 추가 (/api/upload)
"""

from flask import Flask, request, jsonify, send_from_directory
import sys, os, json
from werkzeug.utils import secure_filename

# ===== 경로 설정 =====
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.join(os.path.dirname(__file__), "agents"))

# ===== 내부 모듈 import =====
try:
    from cosine_recommender import run_agent_pipeline
except ImportError as e:
    print("❌ agent3_keywordExtractor.py 불러오기 실패:", e)
    exit()

# ===== Flask 설정 =====
app = Flask(__name__)

# ===== 업로드 폴더 설정 =====
UPLOAD_FOLDER = os.path.join(os.path.dirname(__file__), "static", "uploads")
os.makedirs(UPLOAD_FOLDER, exist_ok=True)
app.config["UPLOAD_FOLDER"] = UPLOAD_FOLDER
ALLOWED_EXTENSIONS = {"png", "jpg", "jpeg", "gif"}


def allowed_file(filename):
    return "." in filename and filename.rsplit(".", 1)[1].lower() in ALLOWED_EXTENSIONS


# ===== 이미지 업로드 API =====
@app.route("/api/upload", methods=["POST"])
def upload_image():
    """
    이미지 파일 업로드 → Flask 서버에 저장 → 접근 가능한 URL 반환
    """
    if "file" not in request.files:
        return jsonify({"error": "file field missing"}), 400

    file = request.files["file"]
    if file.filename == "":
        return jsonify({"error": "no selected file"}), 400

    if not allowed_file(file.filename):
        return jsonify({"error": "unsupported file type"}), 400

    filename = secure_filename(file.filename)
    save_path = os.path.join(app.config["UPLOAD_FOLDER"], filename)
    file.save(save_path)

    # Flask 정적 파일 접근 URL 생성
    file_url = f"http://localhost:5001/static/uploads/{filename}"

    print(f"📸 업로드 완료: {file_url}")
    return jsonify({"url": file_url}), 200


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
        image_urls = data.get("imageUrls", [])

        print(f"\n🚀 [Flask] Received request from Spring (session={session_id})")
        print(f"🗣️  Text: {korean_text}")
        print(f"🖼️  Images: {image_urls}")

        # 이미지 경로 1개만 전달 (여러 개면 첫 번째)
        image_path = image_urls[0] if image_urls else ""

        # --- Agent3 파이프라인 실행 ---
        result = run_agent_pipeline(korean_text=korean_text, image_path=image_path)

        # --- 결과에서 필요한 항목 정리 ---
        english_keywords = result.get("english_keywords", [])
        recommended_songs = result.get("recommended_songs", [])
        merged_sentence = result.get("merged_sentence", "")

        # --- 응답 JSON 생성 ---
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
                for song in result.get("recommended_songs", [])
            ],
        }

        print(f"✅ [Flask] 파이프라인 완료, 키워드={english_keywords}")
        print(f"🎵 추천 결과: {[s.get('track_name') for s in result.get('recommended_songs', [])]}")

        return jsonify(response_data), 200

    except Exception as e:
        print("🔥 서버 내부 오류:", e)
        return jsonify({"error": str(e)}), 500


# ===== 루트 및 헬스체크 =====
@app.route("/", methods=["GET"])
def home():
    return jsonify({"message": "AI Flask Server Running (Agent3 Pipeline)"})


@app.route("/health", methods=["GET"])
def health():
    return jsonify({"status": "ok"}), 200


# ===== 정적 파일 서빙 (업로드된 이미지 접근용) =====
@app.route("/static/uploads/<path:filename>")
def serve_uploaded_file(filename):
    return send_from_directory(app.config["UPLOAD_FOLDER"], filename)


# ===== Flask 실행 =====
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
