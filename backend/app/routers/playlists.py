from fastapi import APIRouter, Depends, HTTPException, Body
from pydantic import BaseModel
from sqlalchemy.orm import Session
from ..models import SessionLocal
from ..services.spotify import get_spotify_client

router = APIRouter(prefix="/playlists", tags=["playlists"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/")
def get_playlists(user_id: str, db: Session = Depends(get_db)):
    sp = get_spotify_client(user_id, db)
    if not sp:
        raise HTTPException(status_code=404, detail="User not found")
    
    playlists = sp.current_user_playlists()
    return playlists['items']

@router.get("/{playlist_id}/tracks")
def get_playlist_tracks(user_id: str, playlist_id: str, db: Session = Depends(get_db)):
    sp = get_spotify_client(user_id, db)
    if not sp:
        raise HTTPException(status_code=404, detail="User not found")
    
    results = sp.playlist_tracks(playlist_id)
    tracks = results['items']
    while results['next']:
        results = sp.next(results)
        tracks.extend(results['items'])
        
    return tracks

@router.post("/create")
def create_playlist(
    user_id: str = Body(...),
    name: str = Body(...),
    track_uris: list[str] = Body(...),
    db: Session = Depends(get_db)
):
    sp = get_spotify_client(user_id, db)
    if not sp:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_profile = sp.current_user()
    playlist = sp.user_playlist_create(user_profile['id'], name, public=False, description="Created by LyricLens")
    
    # Add tracks in batches of 100
    for i in range(0, len(track_uris), 100):
        batch = track_uris[i:i+100]
        sp.playlist_add_items(playlist['id'], batch)
        
    return {"playlistId": playlist['id'], "externalUrl": playlist['external_urls']['spotify']}

class PlaylistCreateRequest(BaseModel):
    name: str
    track_uris: list[str]

class BatchCreateRequest(BaseModel):
    user_id: str
    playlists: list[PlaylistCreateRequest]

@router.post("/batch-create")
def batch_create_playlists(
    request: BatchCreateRequest,
    db: Session = Depends(get_db)
):
    sp = get_spotify_client(request.user_id, db)
    if not sp:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_profile = sp.current_user()
    created_playlists = []
    
    for pl in request.playlists:
        try:
            playlist = sp.user_playlist_create(
                user_profile['id'], 
                pl.name, 
                public=False, 
                description="Created by LyricLens"
            )
            
            # Add tracks in batches of 100
            for i in range(0, len(pl.track_uris), 100):
                batch = pl.track_uris[i:i+100]
                sp.playlist_add_items(playlist['id'], batch)
            
            created_playlists.append({
                "name": pl.name,
                "playlistId": playlist['id'],
                "externalUrl": playlist['external_urls']['spotify']
            })
        except Exception as e:
            print(f"Error creating playlist {pl.name}: {e}")
            
    return created_playlists
