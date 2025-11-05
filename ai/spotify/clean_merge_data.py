# spotify_track_info_merged_all.csv 파일의 결측값 및 아티스트 정보 보완

import pandas as pd
import os

# ===== 파일 경로 =====
DATA_DIR = "spotify/data"
merged_path = os.path.join(DATA_DIR, "spotify_track_info_merged_all.csv")
audio_features_path = os.path.join(DATA_DIR, "audio_features_after2000.csv")

# ===== 1️⃣ 병합된 데이터 로드 =====
merged = pd.read_csv(merged_path)
print(f"📂 merged 데이터 로드 완료: {merged.shape}")

# ===== 2️⃣ track_name, album_name 결측행 제거 =====
# 결측 비율이 각각 0.26%로 매우 낮음
before_drop = len(merged)
merged = merged.dropna(subset=["track_name", "album_name"])
after_drop = len(merged)
print(f"🧹 track_name / album_name 결측행 제거: {before_drop - after_drop}개 삭제 → {after_drop}개 남음")

# ===== 3️⃣ audio_features_after2000.csv 로드 =====
audio_df = pd.read_csv(audio_features_path, usecols=["id", "artists"])
audio_df = audio_df.dropna(subset=["artists"])
print(f"🎶 audio_features 데이터 로드 완료: {audio_df.shape}")

# ===== 4️⃣ artist_name 보완 =====
# 병합 대신 fill 방식으로, merged의 artist_name이 비어있는 부분만 채움
merged = merged.merge(audio_df, on="id", how="left", suffixes=("", "_from_audio"))

# artist_name이 NaN인 경우 → artists 컬럼 값으로 채우기
fill_count = merged["artist_name"].isna().sum()
merged["artist_name"] = merged["artist_name"].fillna(merged["artists"])
after_fill = merged["artist_name"].isna().sum()

print(f"🎨 artist_name 결측 채움 완료: {fill_count - after_fill}개 채워짐 ({after_fill}개 남음)")

# 불필요한 보조 컬럼 제거
if "artists" in merged.columns:
    merged.drop(columns=["artists"], inplace=True)
    
print("\n📊 컬럼별 결측값 개수:")
missing_counts = merged.isna().sum().sort_values(ascending=False)
print(missing_counts[missing_counts > 0])

print("\n📈 결측 비율(%):")
missing_ratio = (merged.isna().mean() * 100).round(2)
print(missing_ratio[missing_ratio > 0])

# ===== 5️⃣ 결과 저장 =====
output_path = os.path.join(DATA_DIR, "spotify_track_info_merged_clean.csv")
merged.to_csv(output_path, index=False, encoding="utf-8-sig")
print(f"\n✅ 최종 저장 완료 → {output_path}")
print(f"📊 최종 shape: {merged.shape}")
## 결측값 없음