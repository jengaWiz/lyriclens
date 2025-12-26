from fastapi import APIRouter, Depends, HTTPException, Body
from sqlalchemy.orm import Session
from ..models import SessionLocal
from ..services.spotify import get_spotify_client
from ..services.genius import GeniusService
from ..services.emotion import EmotionService
from typing import List, Dict, Any

router = APIRouter(prefix="/analyze", tags=["analyze"])
genius_service = GeniusService()
emotion_service = EmotionService()

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.post("/")
async def analyze_playlist(
    user_id: str = Body(...),
    playlist_ids: List[str] = Body(...),
    db: Session = Depends(get_db)
):
    sp = get_spotify_client(user_id, db)
    if not sp:
        raise HTTPException(status_code=404, detail="User not found")

    results = []

    def log_debug(msg):
        with open("debug_log.txt", "a") as f:
            f.write(f"[Router] {msg}\n")

    import sys
    log_debug(f"Python Executable: {sys.executable}")
    log_debug(f"Python Path: {sys.path}")
    log_debug(f"Received analysis request for playlists: {playlist_ids}")

    try:
        for playlist_id in playlist_ids:
            log_debug(f"Processing playlist: {playlist_id}")
            # Fetch tracks
            try:
                playlist_tracks = []
                sp_results = sp.playlist_tracks(playlist_id)
                playlist_tracks.extend(sp_results['items'])
                while sp_results['next']:
                    sp_results = sp.next(sp_results)
                    playlist_tracks.extend(sp_results['items'])
                log_debug(f"Fetched {len(playlist_tracks)} tracks for playlist {playlist_id}")
            except Exception as e:
                log_debug(f"Error fetching tracks for playlist {playlist_id}: {e}")
                print(f"Error fetching tracks for playlist {playlist_id}: {e}")
                continue

            # Process tracks
            track_ids = []
            tracks_map = {}
            
            for item in playlist_tracks:
                track = item.get("track")
                if not track or not track.get("id"):
                    continue
                track_ids.append(track["id"])
                tracks_map[track["id"]] = {
                    "name": track["name"],
                    "artist": track["artists"][0]["name"],
                    "uri": track["uri"],
                    "id": track["id"]
                }
            
            log_debug(f"Found {len(track_ids)} valid track IDs")

            # Fetch Audio Features (batch of 100)
            audio_features_map = {}
            for i in range(0, len(track_ids), 100):
                batch = track_ids[i:i+100]
                try:
                    features = sp.audio_features(batch)
                    for f in features:
                        if f:
                            audio_features_map[f["id"]] = f
                    log_debug(f"Fetched audio features for batch {i}")
                except Exception as e:
                    log_debug(f"Error fetching audio features for batch {i}: {e}")
                    print(f"Error fetching audio features for batch {i}: {e}")
                    # Continue without audio features for this batch
            
            log_debug(f"Total audio features fetched: {len(audio_features_map)}")

            # Analyze each track in parallel
            import asyncio

            async def process_track(track_id, track_data):
                try:
                    # 1. Fetch Lyrics
                    lyrics = await genius_service.fetch_lyrics(track_data["name"], track_data["artist"])
                    
                    # 2. Analyze
                    audio_feat = audio_features_map.get(track_id)
                    emotion = emotion_service.analyze_emotion(
                        track_data["name"], 
                        track_data["artist"], 
                        lyrics, 
                        audio_feat
                    )
                    
                    track_data["emotion"] = emotion
                    return emotion, track_data
                except Exception as e:
                    log_debug(f"Error analyzing track {track_id}: {e}")
                    print(f"Error analyzing track {track_id}: {e}")
                    track_data["emotion"] = "Unknown"
                    return "Unknown", track_data

            tasks = [process_track(tid, tdata) for tid, tdata in tracks_map.items()]
            if tasks:
                processed_tracks = await asyncio.gather(*tasks)
            else:
                processed_tracks = []

            categorized_tracks = {}
            for emotion, track_data in processed_tracks:
                if emotion not in categorized_tracks:
                    categorized_tracks[emotion] = []
                categorized_tracks[emotion].append(track_data)

            results.append({
                "playlistId": playlist_id,
                "results": categorized_tracks
            })

        return results
    except Exception as e:
        log_debug(f"CRITICAL ERROR: {e}")
        import traceback
        traceback.print_exc()
        raise HTTPException(status_code=500, detail=f"Internal Server Error: {str(e)}")
