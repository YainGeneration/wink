"""
Agent 2 (Module)
- Ollama (LLaVA)로 이미지 캡션 생성
"""

import os
import base64
import requests

OLLAMA_URL = "http://localhost:11434"
MODEL_NAME = "llava:latest"

def image_to_english_caption(image_path: str) -> str:
    if not image_path or not os.path.exists(image_path):
        print("❌ Image path invalid")
        return ""
    
    print("🖼️ [Agent 2] Sending image to LLaVA...")

    # 이미지 읽어서 Base64 인코딩
    with open(image_path, "rb") as f:
        image_b64 = base64.b64encode(f.read()).decode("utf-8")

    # ---------------------------------------------------------
    # [수정된 부분] Ollama Native API 포맷에 맞게 Payload 변경
    # ---------------------------------------------------------
    payload = {
        "model": MODEL_NAME,
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


# 테스트용
if __name__ == "__main__":
    path = input("이미지 파일 경로 입력: ").strip()
    # 경로에 따옴표가 섞여 들어올 경우 제거
    path = path.replace("'", "").replace('"', "").strip()
    
    print("🌍 Generated caption:", image_to_english_caption(path))