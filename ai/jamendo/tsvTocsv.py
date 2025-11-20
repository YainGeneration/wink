import pandas as pd

INPUT = "jamendo/data/merged_autotagging_with_meta.tsv"
OUTPUT = "jamendo/data/cleaned_raw_autotagging.csv"

df = pd.read_csv(INPUT, sep="\t")

df["genre_tags"] = df["TAGS"].apply(
    lambda x: " ".join([
        t.replace("genre---", "")
        for t in str(x).split()
        if t.startswith("genre---")
    ])
)

df["instrument_tags"] = df["TAGS"].apply(
    lambda x: " ".join([
        t.replace("instrument---", "")
        for t in str(x).split()
        if t.startswith("instrument---")
    ])
)

df["mood/theme_tags"] = df["TAGS"].apply(
    lambda x: " ".join([
        t.replace("mood/theme---", "")
        for t in str(x).split()
        if t.startswith("mood/theme---")
    ])
)

# RAG가 필요로 하는 컬럼만 남기기
clean = df[[
    "TRACK_ID",
    "PATH",
    "genre_tags",
    "instrument_tags",
    "mood/theme_tags"
]]

clean.to_csv(OUTPUT, index=False)
print("🎉 cleaned_merged_tags.csv 생성 완료:", OUTPUT)
