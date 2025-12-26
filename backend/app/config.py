from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    SPOTIFY_CLIENT_ID: str
    SPOTIFY_CLIENT_SECRET: str
    SPOTIFY_REDIRECT_URI: str
    GENIUS_ACCESS_TOKEN: str
    DATABASE_URL: str = "sqlite:///./lyriclens.db"

    class Config:
        env_file = ".env"

settings = Settings()
