import pandas as pd
import os
from glob import glob

# ===== 1️⃣ 경로 설정 =====
DATA_DIR = "spotify/data"
output_path = os.path.join(DATA_DIR, "spotify_track_info_merged_all.csv")

# ===== 2️⃣ 합칠 파일 목록 지정 =====
file_list = [
    "spotify_track_info_partial_(2011, 2012).csv",
    "spotify_track_info_partial_(2015, 2016).csv",
    "spotify_track_info_partial_(연도:2013~2014).csv",
    "spotify_track_info_partial_(연도:2017~2018).csv",
    "spotify_track_info_partial_ej.csv"
]
file_list = [os.path.join(DATA_DIR, f) for f in file_list]

# 존재하지 않는 파일은 자동 제외
file_list = [f for f in file_list if os.path.exists(f)]

print(f"📂 합칠 파일 수: {len(file_list)}개")
for f in file_list:
    print(f"   - {os.path.basename(f)}")

# ===== 3️⃣ 모든 CSV 읽어서 결합 =====
dfs = []
for path in file_list:
    try:
        df = pd.read_csv(path)
        dfs.append(df)
        print(f"✅ Loaded {os.path.basename(path)} ({len(df)} rows)")
    except Exception as e:
        print(f"⚠️ Failed to read {path}: {e}")

# ===== 4️⃣ 세로로 병합 (컬럼 자동 정렬) =====
merged = pd.concat(dfs, ignore_index=True, sort=False)

print(f"\n🎵 병합 완료 — 총 {len(merged)} rows, {len(merged.columns)} columns")

# ===== 5️⃣ 완전히 중복된 행 제거 =====
before = len(merged)
merged.drop_duplicates(inplace=True)
after = len(merged)
print(f"🧹 완전 중복 행 제거: {before - after}개 제거 → {after}개 남음")

# ===== 6️⃣ 중복 컬럼 이름 제거 =====
merged = merged.loc[:, ~merged.columns.duplicated()]

# ===== ✅ 7️⃣ preview_url 컬럼 제거 =====
if "preview_url" in merged.columns:
    merged.drop(columns=["preview_url"], inplace=True)
    print("🧹 'preview_url' 컬럼 제거 완료.")
else:
    print("⚠️ 'preview_url' 컬럼이 존재하지 않습니다.")
    
# ===== ✅ 8 artists 컬럼 제거 =====
if "artists" in merged.columns:
    merged.drop(columns=["artists"], inplace=True)
    print("🧹 'artists' 컬럼 제거 완료.")
else:
    print("⚠️ 'artists' 컬럼이 존재하지 않습니다.")


# ===== ✅ 8️⃣ 결측값이 있는 행 출력 =====
missing_rows = merged[merged.isna().any(axis=1)]
print(f"\n⚠️ 결측값이 포함된 행: {len(missing_rows)}개")

if not missing_rows.empty:
    print("\n🔍 결측값 포함 행 샘플 (상위 10개):")
    print(missing_rows.head(10))
else:
    print("✅ 결측값이 포함된 행이 없습니다.")
    
print("\n📊 컬럼별 결측값 개수:")
missing_counts = merged.isna().sum().sort_values(ascending=False)
print(missing_counts[missing_counts > 0])

print("\n📈 결측 비율(%):")
missing_ratio = (merged.isna().mean() * 100).round(2)
print(missing_ratio[missing_ratio > 0])

# ===== 9️⃣ 저장 =====
merged.to_csv(output_path, index=False, encoding="utf-8-sig")
print(f"\n✅ 최종 저장 완료 → {output_path}")
print(f"📊 최종 shape: {merged.shape}")

# ===== 🔟 미리보기 =====
print("\n🎧 샘플 5개:")
print(merged.head())




# # 팀원 코드 합치고 na값 제거
# import pandas as pd
# import os

# # ===== 경로 설정 =====
# audio_path = "spotify/data/audio_features_after2000.csv"
# info_path = "spotify/data/spotify_track_info_partial_ej.csv"
# output_path = "spotify/data/merged_tracks_after2000_ej.csv"

# # ===== 1️⃣ CSV 불러오기 =====
# df_audio = pd.read_csv(audio_path)
# df_info = pd.read_csv(info_path)

# print(f"🎧 audio_features: {len(df_audio)} rows, {len(df_audio.columns)} cols")
# print(f"🎶 track_info: {len(df_info)} rows, {len(df_info.columns)} cols")

# # ===== 2️⃣ id 기준 병합 =====
# merged = pd.merge(df_audio, df_info, on="id", how="outer", suffixes=("_audio", "_info"))

# # ===== 3️⃣ 중복 컬럼 통합 =====
# def coalesce(col_a, col_b):
#     """둘 중 하나라도 값이 있으면 채택"""
#     if col_a in merged.columns and col_b in merged.columns:
#         merged[col_a] = merged[col_a].combine_first(merged[col_b])
#         merged.drop(columns=[col_b], inplace=True)

# # artists와 release_date 통합
# coalesce("artists_audio", "artists_info")
# coalesce("release_date_audio", "release_date_info")

# # 이름 정리
# merged.rename(columns={
#     "artists_audio": "artists",
#     "release_date_audio": "release_date"
# }, inplace=True)

# # ===== 4️⃣ 중복 제거 =====
# merged.drop_duplicates(subset=["id"], inplace=True)

# # ===== 5️⃣ 저장 =====
# merged.to_csv(output_path, index=False, encoding="utf-8-sig")

# print(f"\n✅ 병합 및 정리 완료: 총 {len(merged)}개 트랙")
# print(f"📁 저장 위치: {output_path}")

# # ===== 6️⃣ 확인 =====
# print("\n📊 최종 컬럼 목록:")
# print(list(merged.columns))

# print("\n🎵 샘플 3개:")
# print(merged.head(3))
