# ollama_client.py
import requests
import base64
import json
import os

OLLAMA_URL = "http://localhost:11434"
MODEL = "llava"   # llava:latest 자동 매칭됨

def encode_image_to_base64(image_path: str) -> str:
    """이미지 파일을 Base64로 변환"""
    try:
        with open(image_path, "rb") as f:
            return base64.b64encode(f.read()).decode()
    except Exception as e:
        print(f"⚠️ 이미지 Base64 변환 실패: {e}")
        return None


def ask_llava(prompt: str, image_path: str = None) -> str:
    """LLaVA 호출 (이미지 + 텍스트)"""

    # 메시지 구성
    messages = []

    # 이미지 포함 시 멀티파트 메시지 생성
    if image_path and os.path.exists(image_path):
        base64_img = encode_image_to_base64(image_path)
        if base64_img:
            messages.append({
                "role": "user",
                "content": [
                    { "type": "text", "text": prompt },
                    { "type": "image", "image": base64_img }
                ]
            })
        else:
            # 이미지 실패 → 텍스트만
            messages.append({ "role": "user", "content": prompt })
    else:
        # 이미지 없는 경우
        messages.append({ "role": "user", "content": prompt })

    payload = {
        "model": MODEL,
        "messages": messages,
        "stream": False
    }

    try:
        res = requests.post(OLLAMA_URL, json=payload)
        data = res.json()

        # 최신 Ollama JSON 구조
        return data["message"]["content"]

    except Exception as e:
        print("🔥 LLaVA 호출 실패:", e)
        return ""
