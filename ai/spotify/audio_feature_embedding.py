# ai/spotify/audio_feature_embedding.py

import torch
from transformers import AutoTokenizer, AutoModel
import numpy as np
import json
import os

# -----------------------------
# 1) 모델 로드 (전역 캐시)
# -----------------------------
MODEL_NAME = "michellejieli/emotion_text_classifier"
_tokenizer = AutoTokenizer.from_pretrained(MODEL_NAME)
_model = AutoModel.from_pretrained(MODEL_NAME)


# -----------------------------
# 2) 임베딩 함수 정의
# 텍스트 → BERT hidden-state sentence embedding
# -----------------------------
def get_text_embedding(text: str):
    inputs = _tokenizer(text, return_tensors="pt", truncation=True, padding=True)

    with torch.no_grad():
        outputs = _model(**inputs, output_hidden_states=True)

    hidden_state = outputs.hidden_states[-1]  # 마지막 레이어
    embedding = hidden_state.mean(dim=1).squeeze()
    return embedding.cpu().numpy()


# -----------------------------
# 3) Spotify 오디오 피처 설명문 정의
# -----------------------------
FEATURE_DESCRIPTIONS = {
    "acousticness": "A confidence measure from 0.0 to 1.0 of whether the track is acoustic. 1.0 represents high confidence the track is acoustic.",
    "danceability": "Danceability describes how suitable a track is for dancing based on a combination of musical elements including tempo, rhythm stability, beat strength, and overall regularity. A value of 0.0 is least danceable and 1.0 is most danceable.",
    "energy": "Energy is a measure from 0.0 to 1.0 and represents a perceptual measure of intensity and activity. Typically, energetic tracks feel fast, loud, and noisy. For example, death metal has high energy, while a Bach prelude scores low on the scale. Perceptual features contributing to this attribute include dynamic range, perceived loudness, timbre, onset rate, and general entropy.",
    "instrumentalness": "Predicts whether a track contains no vocals. 'Ooh' and 'aah' sounds are treated as instrumental in this context. Rap or spoken word tracks are clearly 'vocal'. The closer the instrumentalness value is to 1.0, the greater likelihood the track contains no vocal content. Values above 0.5 are intended to represent instrumental tracks, but confidence is higher as the value approaches 1.0.",
    "key": "The key the track is in. Integers map to pitches using standard Pitch Class notation. E.g. 0 = C, 1 = C♯/D♭, 2 = D, and so on. If no key was detected, the value is -1.",
    "liveness": "Detects the presence of an audience in the recording. Higher liveness values represent an increased probability that the track was performed live. A value above 0.8 provides strong likelihood that the track is live.",
    "loudness": "The overall loudness of a track in decibels (dB). Loudness values are averaged across the entire track and are useful for comparing relative loudness of tracks. Loudness is the quality of a sound that is the primary psychological correlate of physical strength (amplitude). Values typically range between -60 and 0 db.",
    "mode": "Mode indicates the modality (major or minor) of a track, the type of scale from which its melodic content is derived. Major is represented by 1 and minor is 0.",    
    "speechiness": "Speechiness detects the presence of spoken words in a track. The more exclusively speech-like the recording (e.g. talk show, audio book, poetry), the closer to 1.0 the attribute value. Values above 0.66 describe tracks that are probably made entirely of spoken words.Values between 0.33 and 0.66 describe tracks that may contain both music and speech, either in sections or layered, including such cases as rap music. Values below 0.33 most likely represent music and other non-speech-like tracks.",
    "valence": "A measure from 0.0 to 1.0 describing the musical positiveness conveyed by a track. Tracks with high valence sound more positive (e.g. happy, cheerful, euphoric), while tracks with low valence sound more negative (e.g. sad, depressed, angry).",
    "tempo": "The overall estimated tempo of a track in beats per minute (BPM). In musical terminology, tempo is the speed or pace of a given piece and derives directly from the average beat duration.",
}

# -----------------------------
# 4) 설명문 → 임베딩 생성 및 저장
# -----------------------------
def build_audio_feature_embeddings(save_path="spotify/embedding_data/audio_feature_embeddings.json"):
    final = {}

    for feature, desc in FEATURE_DESCRIPTIONS.items():
        emb = get_text_embedding(desc)
        final[feature] = emb.tolist()

    os.makedirs(os.path.dirname(save_path), exist_ok=True)

    with open(save_path, "w", encoding="utf-8") as f:
        json.dump(final, f, ensure_ascii=False, indent=2)

    print(f"✅ 오디오 피처 임베딩 저장 완료 → {save_path}")


if __name__ == "__main__":
    build_audio_feature_embeddings()