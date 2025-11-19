# ===== id를 기준으로 새로운 데이터셋을 만드는 스크립트 =====

import spotipy
from spotipy.oauth2 import SpotifyOAuth
import pandas as pd
from tqdm import tqdm
import time
import os

# ===== Spotify 인증 =====
sp = spotipy.Spotify(auth_manager=SpotifyOAuth(
    client_id='9f601ae991474c5f9acbbca99f0d9c7c',
    client_secret='302529b448714aaabc311bdb65772a96',
    redirect_uri='http://127.0.0.1:8888/callback',
    scope='user-library-read'
))

# ===== 1. CSV 불러오기 =====
input_path = os.path.join("spotify/data/audio_features_after2000.csv")
df = pd.read_csv(input_path)

# 팀원 별 연도 필터 설정
# df = df[df["year"].between(2015, 2016)]  # 예인
# df = df[df["year"].between(2017, 2018)]  # 다은
df = df[df["year"].between(2019, 2020)]  # 은정


# 중복 제거
track_ids = df['id'].dropna().unique().tolist()
# 본인이 맡은 구간으로 프린트문 수정
print(f"🎧 총 {len(track_ids)}개의 트랙 ID를 불러왔습니다. (연도: )\n")

# ===== 2. 배치 설정 =====
batch_size = 100  # 한 번에 100개씩 요청
results = []
failed_ids = []

# ===== 3. Spotify 트랙 정보 수집 (Batch 단위) =====
for start in range(0, len(track_ids), batch_size):
    sub_ids = track_ids[start:start + batch_size]
    batch_num = start // batch_size + 1
    print(f"\n🚀 Batch {batch_num} 처리 중... ({len(sub_ids)} tracks)")

    for tid in tqdm(sub_ids, desc=f"Batch {batch_num}"):
        try:
            track = sp.track(tid)
            info = {
                "id": tid, # 트랙 ID
                "track_name": track["name"], # 트랙 이름
                "artist_name": track["artists"][0]["name"], # 아티스트 이름
                "album_name": track["album"]["name"], # 앨범 이름
                "external_url": track["external_urls"]["spotify"], # 스포티파이 URL
                "preview_url": track["preview_url"], # 미리듣기 URL
                "release_date": track["album"]["release_date"],   # 발매일
            }
            results.append(info)
            time.sleep(0.3)  # ✅ 요청 간 대기 (rate limit 방지)
        except spotipy.exceptions.SpotifyException as e:
            # Rate limit 감지 시 잠시 대기 후 계속 진행
            if "rate" in str(e).lower() or "429" in str(e):
                print("⚠️ Rate limit hit. 60초 대기 중...")
                time.sleep(60)
                continue
            else:
                print(f"❌ Failed: {tid} ({e})")
                failed_ids.append(tid)
                continue
        except Exception as e:
            print(f"⚠️ Unexpected error: {tid} ({e})")
            failed_ids.append(tid)
            continue

    # 🔹 배치별 중간 저장
    temp_df = pd.DataFrame(results)
    # 각자 맡은 연도 입력(파일 저장)
    temp_path = os.path.join("spotify/data/spotify_track_info_partial_(각자 맡은 연도 넣기).csv")
    temp_df.to_csv(temp_path, index=False)
    print(f"💾 Batch {batch_num} 저장 완료 ({len(temp_df)}곡 누적)")

# ===== 4. 최종 저장 =====
# 각자 맡은 연도 입력(파일 저장)
output_path = os.path.join("spotify/data/spotify_track_info_after2000_(각자 맡은 연도 넣기).csv")
result_df = pd.DataFrame(results)
result_df.to_csv(output_path, index=False)

print(f"\n✅ 모든 배치 처리 완료! 총 {len(result_df)}개 트랙 정보 저장")
print(f"📁 최종 파일: {output_path}")

# ===== 5. 실패한 트랙 저장 =====
# 각자 맡은 연도 입력(파일 저장)
if failed_ids:
    fail_path = os.path.join("spotify/data/failed_ids_after2000_(각자 맡은 연도 넣기).csv")
    pd.DataFrame({"failed_id": failed_ids}).to_csv(fail_path, index=False)
    print(f"🚨 실패한 트랙 {len(failed_ids)}개 → {fail_path} 저장 완료")
