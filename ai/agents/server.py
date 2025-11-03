# server.py
# -*- coding: utf-8 -*-
"""
Flask API for AI Recommendation
- Agent3 통합 파이프라인(run_agent_pipeline) 직접 호출
- Spring Boot (http://localhost:8080) → Flask (http://127.0.0.1:5001) 연결용
"""

from flask import Flask, request, jsonify
import sys, os, json

# ===== 경로 설정 =====
sys.path.append(os.path.dirname(__file__))
sys.path.append(os.path.join(os.path.dirname(__file__), "agents"))

# ===== 내부 모듈 import =====
try:
    from agent3_keywordExtractor import run_agent_pipeline
except ImportError as e:
    print("❌ agent3_keywordExtractor.py 불러오기 실패:", e)
    exit()

# ===== Flask 설정 =====
app = Flask(__name__)

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
            "aiMessage": f"'{topic}' 감성에 어울리는 음악을 추천합니다.",
            "keywords": english_keywords,
            "recommendations": recommended_songs,
            "mergedSentence": merged_sentence,
        }

        print(f"✅ [Flask] 파이프라인 완료, 키워드={english_keywords}")
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


# ===== Flask 실행 =====
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5001)
