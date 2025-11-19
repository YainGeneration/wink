# RAGdb에 넣기 위해 csv 파일 생성

import pandas as pd

# 입력 파일
merged_path = "jamendo/data/merged_autotagging_with_meta.tsv"
cleaned_path = "jamendo/data/cleaned_raw_autotagging.csv"

# 출력 파일
OUTPUT = "jamendo/data/final_jamendo_metadata.csv"

print("\n--- 최종 Jamendo 메타데이터 결합 시작 ---")

# 1. merged 파일 로드
merged_df = pd.read_csv(merged_path, sep="\t")

# 2. cleaned 태그 파일 로드
clean_df = pd.read_csv(cleaned_path)

# 3. TAGS 컬럼 제거 (필요 없으니까 삭제)
if "TAGS" in merged_df.columns:
    merged_df = merged_df.drop(columns=["TAGS"])

# 4. TRACK_ID 기준 병합
final_df = merged_df.merge(
    clean_df[["TRACK_ID", "genre_tags", "instrument_tags", "mood/theme_tags"]],
    on="TRACK_ID",
    how="left"
)

# 5. 컬럼 순서 네가 원하는대로 정렬
final_df = final_df[
    [
        "TRACK_ID",
        "ARTIST_ID",
        "ALBUM_ID",
        "PATH",
        "DURATION",
        "genre_tags",
        "instrument_tags",
        "mood/theme_tags",
        "TRACK_NAME",
        "ARTIST_NAME",
        "ALBUM_NAME",
        "RELEASEDATE",
        "URL",
    ]
]

# 6. 최종 저장
final_df.to_csv(OUTPUT, index=False)

print("🎉 최종 파일 생성 완료 →", OUTPUT)
print("총 행:", len(final_df))
print("총 컬럼:", len(final_df.columns))
