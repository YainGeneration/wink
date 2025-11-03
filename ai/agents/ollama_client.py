import requests
import json
import ast  # 안전한 문자열 → 객체 변환용

OLLAMA_URL = "http://localhost:11434/api/generate"
MODEL_NAME = "gemma3:27b"

def query_ollama(prompt: str) -> str:
    payload = {"model": MODEL_NAME, "prompt": prompt}
    response = requests.post(OLLAMA_URL, json=payload, stream=True)

    output_parts = []  # 안전하게 문자열 조각 누적
    for line in response.iter_lines():
        if line:
            try:
                data = json.loads(line)
                resp = data.get("response", "")
                if isinstance(resp, list):
                    resp = " ".join(map(str, resp))
                elif not isinstance(resp, str):
                    resp = str(resp)
                output_parts.append(resp)
            except Exception as e:
                print("⚠️ JSON 파싱 실패:", e)
                continue

    output = " ".join(output_parts).strip()

    # ✅ Ollama가 리스트 형태 문자열을 보낼 경우에도 방어
    try:
        parsed = ast.literal_eval(output)
        if isinstance(parsed, list):
            output = " ".join(map(str, parsed))
    except Exception:
        pass

    return output
