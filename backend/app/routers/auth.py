from fastapi import APIRouter, Depends, HTTPException, Request
from fastapi.responses import RedirectResponse
from sqlalchemy.orm import Session
import httpx
import base64
import time
from ..config import settings
from ..models import SessionLocal, User

router = APIRouter(prefix="/auth", tags=["auth"])

def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

@router.get("/login")
def login():
    scope = "user-library-read playlist-read-private playlist-modify-private playlist-modify-public user-read-private user-read-email"
    params = {
        "client_id": settings.SPOTIFY_CLIENT_ID,
        "response_type": "code",
        "redirect_uri": settings.SPOTIFY_REDIRECT_URI,
        "scope": scope,
    }
    url = f"https://accounts.spotify.com/authorize?client_id={params['client_id']}&response_type={params['response_type']}&redirect_uri={params['redirect_uri']}&scope={params['scope']}"
    return RedirectResponse(url)

@router.get("/callback")
async def callback(code: str, db: Session = Depends(get_db)):
    async with httpx.AsyncClient() as client:
        # Exchange code for token
        auth_header = base64.b64encode(f"{settings.SPOTIFY_CLIENT_ID}:{settings.SPOTIFY_CLIENT_SECRET}".encode()).decode()
        headers = {
            "Authorization": f"Basic {auth_header}",
            "Content-Type": "application/x-www-form-urlencoded"
        }
        data = {
            "grant_type": "authorization_code",
            "code": code,
            "redirect_uri": settings.SPOTIFY_REDIRECT_URI
        }
        
        response = await client.post("https://accounts.spotify.com/api/token", headers=headers, data=data)
        if response.status_code != 200:
            raise HTTPException(status_code=400, detail="Failed to retrieve token from Spotify")
        
        token_data = response.json()
        access_token = token_data["access_token"]
        refresh_token = token_data.get("refresh_token")
        expires_in = token_data["expires_in"]
        
        # Get User Profile
        user_headers = {"Authorization": f"Bearer {access_token}"}
        user_response = await client.get("https://api.spotify.com/v1/me", headers=user_headers)
        if user_response.status_code != 200:
             raise HTTPException(status_code=400, detail="Failed to retrieve user profile")
        
        user_data = user_response.json()
        spotify_id = user_data["id"]
        
        # Save/Update User
        user = db.query(User).filter(User.spotify_id == spotify_id).first()
        if not user:
            user = User(spotify_id=spotify_id)
            db.add(user)
        
        user.access_token = access_token
        if refresh_token:
            user.refresh_token = refresh_token
        user.expires_at = int(time.time()) + expires_in
        
        db.commit()
        db.refresh(user)
        
        # Redirect to frontend
        return RedirectResponse(f"http://127.0.0.1:5173/dashboard?user={spotify_id}")

@router.get("/users/{user_id}")
async def get_user_profile(user_id: str, db: Session = Depends(get_db)):
    from ..services.spotify import get_spotify_client
    sp = get_spotify_client(user_id, db)
    if not sp:
        raise HTTPException(status_code=404, detail="User not found")
    
    user_profile = sp.current_user()
    playlists = sp.current_user_playlists()
    
    return {
        "user": user_profile,
        "playlists": playlists['items']
    }
