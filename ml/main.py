from fastapi import FastAPI
from pydantic import BaseModel

app = FastAPI()

class LyricsRequest(BaseModel):
    lyrics: str

@app.post("/analyze")
def analyze_lyrics(request: LyricsRequest):
    if "love" in request.lyrics.lower():
        return {"emotion": "happy"}
    else:
        return {"emotion": "neutral"}