# autotagging.tsv
# raw.meta.tsv

import re
import pandas as pd

# ----------------------------
# 파일 경로
# ----------------------------
FILE_AUTO = "mtg-jamendo-dataset/data/autotagging.tsv"
FILE_META = "mtg-jamendo-dataset/data/raw.meta.tsv"
OUTPUT = "jamendo/data/merged_autotagging_with_meta.tsv"

print("\n--- Jamendo 데이터 파싱 + 병합 시작 ---")

# ============================================================
# 1. autotagging.tsv 안전 파싱
# ============================================================
rows = []
tag_pattern = re.compile(r"(genre---\S+|instrument---\S+)")

with open(FILE_AUTO, "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line:
            continue

        parts = line.split("\t")

        if len(parts) < 5:
            continue

        track_id, artist_id, album_id, path, duration = parts[:5]
        tags = tag_pattern.findall(line)

        rows.append({
            "TRACK_ID": track_id,
            "ARTIST_ID": artist_id,
            "ALBUM_ID": album_id,
            "PATH": path,
            "DURATION": duration,
            "TAGS": " ".join(tags)
        })

auto_df = pd.DataFrame(rows)
print(f"✅ autotagging 파싱 완료: {len(auto_df)}개 행")


# ============================================================
# 2. raw.meta.tsv 안전 파싱
# ============================================================

meta_rows = []

with open(FILE_META, "r", encoding="utf-8") as f:
    for line in f:
        line = line.strip()
        if not line:
            continue

        # 앞 3개(ID 부분)는 탭으로 split
        parts = line.split("\t")

        if len(parts) < 3:
            continue

        track_id, artist_id, album_id = parts[:3]

        # 나머지 필드는 탭이 깨져 있을 위험 → regex로 추출
        # TRACK_NAME은 ARTIST_ID 이후 첫 번째 문자열
        # ARTIST_NAME, ALBUM_NAME도 같은 방식
        # 가장 안전한 방식: 탭 split 후 부족한 경우 문자열 조합하여 다시 패턴 매칭

        # 모든 텍스트를 다시 잡아온 뒤 5개 필드를 정규식으로 추출
        # format: TRACK_ID ARTIST_ID ALBUM_ID TRACK_NAME ARTIST_NAME ALBUM_NAME RELEASEDATE URL
        # TRACK_NAME~URL은 탭/스페이스 섞여 있을 수 있으므로, 뒤에서부터 5개를 재구성
        tail = line[len(track_id) + len(artist_id) + len(album_id) + 3:]

        # 뒤에서부터 5필드를 잡아내는 패턴
        meta_pattern = r"(.+?)\t(.+?)\t(.+?)\t(.+?)\t(.+)$"
        m = re.search(meta_pattern, tail)

        if not m:
            continue

        track_name, artist_name, album_name, releasedate, url = m.groups()

        meta_rows.append({
            "TRACK_ID": track_id,
            "TRACK_NAME": track_name,
            "ARTIST_NAME": artist_name,
            "ALBUM_NAME": album_name,
            "RELEASEDATE": releasedate,
            "URL": url
        })

meta_df = pd.DataFrame(meta_rows)
print(f"✅ raw.meta 파싱 완료: {len(meta_df)}개 행")


# ============================================================
# 3. TRACK_ID 기준 병합
# ============================================================
merged = auto_df.merge(meta_df, on="TRACK_ID", how="left")
print(f"🔗 병합 완료: {len(merged)}개 행")

# 누락 확인
missing = merged[merged["TRACK_NAME"].isna()]
print(f"⚠ 메타데이터 매칭 실패: {len(missing)}개 TRACK_ID")


# ============================================================
# 4. 저장
# ============================================================
merged.to_csv(OUTPUT, sep="\t", index=False)
print(f"🎉 최종 merged 저장 완료 → {OUTPUT}\n")
