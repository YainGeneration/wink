# filter_jamendo_paths.py

import pandas as pd

INPUT_CSV = "jamendo/data/final_jamendo_metadata.csv"
OUTPUT_CSV = "jamendo/data/final_jamendo_metadata_filtered.csv"


def extract_prefix(path):
    """
    경로 PATH에서 앞 두 자리 숫자(폴더명)만 추출
    예) 78/915578.mp3 → 78
        05/123456.mp3 → 5
    """
    try:
        prefix = path.split("/")[0]
        return int(prefix)
    except:
        return None


def filter_metadata():
    df = pd.read_csv(INPUT_CSV)

    # PATH 앞 두 자리 숫자 추출
    df["prefix"] = df["PATH"].apply(extract_prefix)

    # prefix가 0~51 사이만 유지
    df_filtered = df[df["prefix"].between(0, 51)]

    # prefix 컬럼 제거
    df_filtered = df_filtered.drop(columns=["prefix"])

    # 저장
    df_filtered.to_csv(OUTPUT_CSV, index=False)
    print(f"🎉 필터링 완료! {len(df_filtered)}개 트랙 저장 → {OUTPUT_CSV}")


if __name__ == "__main__":
    filter_metadata()
