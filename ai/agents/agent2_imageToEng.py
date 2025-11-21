"""
Agent 2 (Module)
- Ollama (Gemma3)로 이미지 캡션 생성
이미지
장소 처리
"""

import os
import base64
import requests

OLLAMA_URL = "http://localhost:11434"
MODEL_NAME = "gemma3:4b"   

def image_to_english_caption(image_path: str) -> str:
    if not image_path or not os.path.exists(image_path):
        print("❌ Image path invalid")
        return ""
    
    print("🖼️ [Agent 2] Sending image to Gemma3:4b...")
    
    prompt = """
You are an AI that describes the content and atmosphere of an image.
Your task is to generate **one natural-sounding caption sentence**.

RULES:
- Describe ONLY what you see.
- Focus on mood and atmosphere.
- Use ONE sentence only.
- No introductions such as “This image shows...”
- No questions.
- No formatting, no bullet points, no line breaks.
- No quotes.

Return only the caption sentence.
"""

    # 이미지 읽어서 Base64 인코딩
    with open(image_path, "rb") as f:
        image_b64 = base64.b64encode(f.read()).decode("utf-8")

    # ---------------------------------------------------------
    # [수정된 부분] Ollama Native API 포맷에 맞게 Payload 변경
    # ---------------------------------------------------------
    payload = {
        "model": MODEL_NAME,
        "prompt": prompt,
        "images": [image_b64],
        "messages": [
            {
                "role": "user",
                "content": "Describe this image in one natural English sentence focusing on mood and atmosphere.",
                "images": [image_b64]  # content 리스트가 아니라 별도의 images 키 사용
            }
        ],
        "stream": False
    }

    try:
        res = requests.post(f"{OLLAMA_URL}/api/chat", json=payload, timeout=90)
        res.raise_for_status() # 400, 500 에러 발생 시 예외 발생
        data = res.json()
        # print("📡 Raw Response:", data) # 디버깅 시 주석 해제

    except Exception as e:
        print(f"🔥 Ollama request failed: {e}")
        if 'res' in locals():
             print(f"👉 Server replied: {res.text}") # 서버가 보낸 구체적인 에러 메시지 확인
        return ""

    # LLaVA 응답에서 텍스트만 추출
    message = data.get("message", {})
    content = message.get("content", "")

    caption = content.strip()
    caption = caption.replace('"', '').replace("'", "").strip()

    return caption

# 캡션 생성 (Base64 입력 버전)
def caption_from_base64(image_base64: str) -> str:
    if not image_base64:
        print("❌ No base64 input")
        return ""

    # Remove prefix like: data:image/png;base64,
    if image_base64.startswith("data:image"):
        image_base64 = image_base64.split(",")[1]

    # Remove whitespaces/newlines
    image_base64 = image_base64.replace("\n", "").replace(" ", "").strip()

    # Validate base64
    try:
        base64.b64decode(image_base64)
    except:
        print("❌ Invalid Base64 format")
        return ""

    print("🖼️ [Agent 2] Caption from Base64 image...")

    payload = {
        "model": MODEL_NAME,
        "messages": [{
            "role": "user",
            "content": "Describe this image in natural English.",
            "images": [image_base64]
        }],
        "stream": False
    }

    try:
        res = requests.post(f"{OLLAMA_URL}/api/chat", json=payload, timeout=90)
        res.raise_for_status()
        data = res.json()
        caption = data.get("message", {}).get("content", "").strip()
        return caption
    except Exception as e:
        print(f"🔥 Base64 caption generation failed: {e}")
        if 'res' in locals():
            print("👉 Server replied:", res.text)
        return ""

# ---------------------------------------------------------
# (추가 2) 위치 기반 보정: 캡션에 장소 분위기 반영
# ---------------------------------------------------------
def enhance_caption_with_location(caption: str, place_name: str) -> str:
    """
    이미지 캡션 + 장소명을 조합하여 더 정교한 문맥 생성.
    예:
        caption="A snowy street with warm lights"
        place_name="홍순언 앞 거리"
        →
        "A snowy street with warm lights near Hongsoon-eon Street"
    """

    if not caption:
        return caption

    if not place_name:
        return caption

    enhanced = f"{caption}, taken near {place_name}."

    return enhanced.strip()

# 메인 테스트
if __name__ == "__main__":
    import json

    print("\n🧪 === Agent2 위치 기반 캡션 생성 테스트 ===\n")

    # 1) 파일 경로 → Base64 변환
    image_path = "/Users/eunjung/Desktop/wink/ai/images/snowman.png"
    with open(image_path, "rb") as f:
        b64 = base64.b64encode(f.read()).decode()

    # 2) 실제 백엔드 형식으로 전달
    test_payload = {
        "imageBase64": [b64],
        "location": {
            "lat": 37.55,
            "lng": 126.97,
            "address": "서울시 용산구 한강대로",
            "placeName": "홍순언 앞 거리"
        },
        "nearbyMusic": [
            {"songId": 551, "title": "Love Dive", "artist": "IVE"},
            {"songId": 552, "title": "Hype Boy", "artist": "NewJeans"}
        ]
    }

    # 3) Base64 → 캡션 생성
    print("📌 Step 1: Base64 → Caption 생성 중...")
    caption = caption_from_base64(test_payload["imageBase64"][0])
    print("👉 Caption:", caption)

    # 4) 장소 기반 보정
    place_name = test_payload["location"]["placeName"]
    enhanced_caption = enhance_caption_with_location(caption, place_name)
    print("👉 Enhanced Caption:", enhanced_caption)
