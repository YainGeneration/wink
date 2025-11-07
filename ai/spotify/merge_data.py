# # 전체 데이터 합치기(오디오 피처 병합 전&클린 전)
# import pandas as pd
# import os
# from glob import glob

# # ===== 1️⃣ 경로 설정 =====
# DATA_DIR = "spotify/data"
# output_path = os.path.join(DATA_DIR, "spotify_track_info_merged_all.csv")

# # ===== 2️⃣ 합칠 파일 목록 지정 =====
# file_list = [
#     "spotify_track_info_partial_(2011, 2012).csv",
#     "spotify_track_info_partial_(2015, 2016).csv",
#     "spotify_track_info_partial_(연도:2013~2014).csv",
#     "spotify_track_info_partial_(연도:2017~2018).csv",
#     "spotify_track_info_partial_ej.csv"
# ]
# file_list = [os.path.join(DATA_DIR, f) for f in file_list]

# # 존재하지 않는 파일은 자동 제외
# file_list = [f for f in file_list if os.path.exists(f)]

# print(f"📂 합칠 파일 수: {len(file_list)}개")
# for f in file_list:
#     print(f"   - {os.path.basename(f)}")

# # ===== 3️⃣ 모든 CSV 읽어서 결합 =====
# dfs = []
# for path in file_list:
#     try:
#         df = pd.read_csv(path)
#         dfs.append(df)
#         print(f"✅ Loaded {os.path.basename(path)} ({len(df)} rows)")
#     except Exception as e:
#         print(f"⚠️ Failed to read {path}: {e}")

# # ===== 4️⃣ 세로로 병합 (컬럼 자동 정렬) =====
# merged = pd.concat(dfs, ignore_index=True, sort=False)

# print(f"\n🎵 병합 완료 — 총 {len(merged)} rows, {len(merged.columns)} columns")

# # ===== 5️⃣ 완전히 중복된 행 제거 =====
# before = len(merged)
# merged.drop_duplicates(inplace=True)
# after = len(merged)
# print(f"🧹 완전 중복 행 제거: {before - after}개 제거 → {after}개 남음")

# # ===== 6️⃣ 중복 컬럼 이름 제거 =====
# merged = merged.loc[:, ~merged.columns.duplicated()]

# # ===== ✅ 7️⃣ preview_url 컬럼 제거 =====
# if "preview_url" in merged.columns:
#     merged.drop(columns=["preview_url"], inplace=True)
#     print("🧹 'preview_url' 컬럼 제거 완료.")
# else:
#     print("⚠️ 'preview_url' 컬럼이 존재하지 않습니다.")
    
# # ===== ✅ 8 artists 컬럼 제거 =====
# if "artists" in merged.columns:
#     merged.drop(columns=["artists"], inplace=True)
#     print("🧹 'artists' 컬럼 제거 완료.")
# else:
#     print("⚠️ 'artists' 컬럼이 존재하지 않습니다.")


# # ===== ✅ 8️⃣ 결측값이 있는 행 출력 =====
# missing_rows = merged[merged.isna().any(axis=1)]
# print(f"\n⚠️ 결측값이 포함된 행: {len(missing_rows)}개")

# if not missing_rows.empty:
#     print("\n🔍 결측값 포함 행 샘플 (상위 10개):")
#     print(missing_rows.head(10))
# else:
#     print("✅ 결측값이 포함된 행이 없습니다.")
    
# print("\n📊 컬럼별 결측값 개수:")
# missing_counts = merged.isna().sum().sort_values(ascending=False)
# print(missing_counts[missing_counts > 0])

# print("\n📈 결측 비율(%):")
# missing_ratio = (merged.isna().mean() * 100).round(2)
# print(missing_ratio[missing_ratio > 0])

# # ===== 9️⃣ 저장 =====
# merged.to_csv(output_path, index=False, encoding="utf-8-sig")
# print(f"\n✅ 최종 저장 완료 → {output_path}")
# print(f"📊 최종 shape: {merged.shape}")

# # ===== 🔟 미리보기 =====
# print("\n🎧 샘플 5개:")
# print(merged.head())




# 팀원 코드 합치고 na값 제거 + 중복 컬럼 정리
import pandas as pd
import os
import ast

# ===== 경로 설정 =====
DATA_DIR = "spotify/data"
features_path = os.path.join(DATA_DIR, "audio_features_after2000.csv")
info_path = os.path.join(DATA_DIR, "spotify_track_info_merged_clean.csv")
output_path = os.path.join(DATA_DIR, "merged_audio_features_after2000_final.csv")

# ===== 1️⃣ CSV 불러오기 =====
features = pd.read_csv(features_path)
info = pd.read_csv(info_path)

# ===== 2️⃣ artists 컬럼 정리 =====
def clean_artists(value):
    """['Artist'] 형식을 문자열로 변환"""
    if pd.isna(value):
        return None
    try:
        parsed = ast.literal_eval(value) if isinstance(value, str) else value
        if isinstance(parsed, list):
            return ", ".join(parsed)
        return str(parsed)
    except Exception:
        return str(value)

if "artists" in features.columns:
    features["artists"] = features["artists"].apply(clean_artists)

# ===== 3️⃣ 병합 =====
merged = info.merge(features, on="id", how="left")

# ===== 4️⃣ 중복 컬럼 정리 =====
for col in merged.columns:
    if col.endswith("_x"):
        base = col[:-2]
        if f"{base}_y" in merged.columns:
            merged.drop(columns=[f"{base}_y"], inplace=True)
        merged.rename(columns={col: base}, inplace=True)

# ===== 5️⃣ artist_name 결측 보완 및 정리 =====
if "artist_name" in merged.columns and "artists" in merged.columns:
    merged["artist_name"] = merged["artist_name"].fillna(merged["artists"])
    merged.drop(columns=["artists"], inplace=True)

# ===== 🎯 NEW: artist_name 문자열 클린업 =====
def clean_artist_name(val):
    if pd.isna(val):
        return None
    # 리스트 문자열로 되어 있으면 literal_eval로 파싱
    try:
        parsed = ast.literal_eval(val)
        if isinstance(parsed, list):
            val = ", ".join(parsed)
    except Exception:
        pass
    # 대괄호/따옴표/여분 공백 제거
    val = str(val)
    val = val.replace("[", "").replace("]", "").replace("'", "").replace('"', "")
    val = val.strip()
    return val

merged["artist_name"] = merged["artist_name"].apply(clean_artist_name)

# ===== 6️⃣ 불필요 컬럼 제거 =====
drop_cols = [c for c in ["year", "name"] if c in merged.columns]
if drop_cols:
    merged.drop(columns=drop_cols, inplace=True)

# ===== 7️⃣ 결측 행 제거 =====
merged.dropna(inplace=True)

# ===== 8️⃣ 저장 =====
merged.to_csv(output_path, index=False, encoding="utf-8-sig")

print(f"\n✅ 최종 저장 완료 → {output_path}")
print(f"📊 최종 shape: {merged.shape}")
print("🎨 artist_name 컬럼이 대괄호 없는 순수 문자열로 정리되었습니다.")
