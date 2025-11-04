# 팀원 코드 합치고 na값 제거
import pandas as pd
import os

# ===== 경로 설정 =====
audio_path = "spotify/data/audio_features_after2000.csv"
info_path = "spotify/data/spotify_track_info_partial_ej.csv"
output_path = "spotify/data/merged_tracks_after2000_ej.csv"

# ===== 1️⃣ CSV 불러오기 =====
df_audio = pd.read_csv(audio_path)
df_info = pd.read_csv(info_path)

print(f"🎧 audio_features: {len(df_audio)} rows, {len(df_audio.columns)} cols")
print(f"🎶 track_info: {len(df_info)} rows, {len(df_info.columns)} cols")

# ===== 2️⃣ id 기준 병합 =====
merged = pd.merge(df_audio, df_info, on="id", how="outer", suffixes=("_audio", "_info"))

# ===== 3️⃣ 중복 컬럼 통합 =====
def coalesce(col_a, col_b):
    """둘 중 하나라도 값이 있으면 채택"""
    if col_a in merged.columns and col_b in merged.columns:
        merged[col_a] = merged[col_a].combine_first(merged[col_b])
        merged.drop(columns=[col_b], inplace=True)

# artists와 release_date 통합
coalesce("artists_audio", "artists_info")
coalesce("release_date_audio", "release_date_info")

# 이름 정리
merged.rename(columns={
    "artists_audio": "artists",
    "release_date_audio": "release_date"
}, inplace=True)

# ===== 4️⃣ 중복 제거 =====
merged.drop_duplicates(subset=["id"], inplace=True)

# ===== 5️⃣ 저장 =====
merged.to_csv(output_path, index=False, encoding="utf-8-sig")

print(f"\n✅ 병합 및 정리 완료: 총 {len(merged)}개 트랙")
print(f"📁 저장 위치: {output_path}")

# ===== 6️⃣ 확인 =====
print("\n📊 최종 컬럼 목록:")
print(list(merged.columns))

print("\n🎵 샘플 3개:")
print(merged.head(3))
