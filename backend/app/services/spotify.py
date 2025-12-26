import spotipy
from spotipy.oauth2 import SpotifyOAuth
from sqlalchemy.orm import Session
from ..models import User
from ..config import settings
import time

def get_spotify_client(user_id: str, db: Session):
    user = db.query(User).filter(User.spotify_id == user_id).first()
    if not user:
        return None

    # Check if token is expired
    if user.expires_at < time.time():
        sp_oauth = SpotifyOAuth(
            client_id=settings.SPOTIFY_CLIENT_ID,
            client_secret=settings.SPOTIFY_CLIENT_SECRET,
            redirect_uri=settings.SPOTIFY_REDIRECT_URI
        )
        token_info = sp_oauth.refresh_access_token(user.refresh_token)
        user.access_token = token_info['access_token']
        user.expires_at = int(time.time()) + token_info['expires_in']
        db.commit()
        db.refresh(user)

    return spotipy.Spotify(auth=user.access_token)
