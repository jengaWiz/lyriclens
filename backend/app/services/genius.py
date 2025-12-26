import httpx
from bs4 import BeautifulSoup
import re
from ..config import settings

class GeniusService:
    def __init__(self):
        self.base_url = "https://api.genius.com"
        self.headers = {"Authorization": f"Bearer {settings.GENIUS_ACCESS_TOKEN}"}

    def clean_title(self, title: str) -> str:
        # Remove features and parentheses
        title = re.sub(r"\(feat\..*?\)", "", title)
        title = re.sub(r"\(with.*?\)", "", title)
        title = re.sub(r"\[feat\..*?\]", "", title)
        title = re.sub(r"\(.*?\)", "", title)
        title = re.sub(r"\[.*?\]", "", title)
        return title.strip()

    async def fetch_lyrics(self, title: str, artist: str) -> str:
        if not settings.GENIUS_ACCESS_TOKEN or settings.GENIUS_ACCESS_TOKEN == "dummy_token":
            return None

        clean_title = self.clean_title(title)
        search_queries = [f"{clean_title} {artist}", clean_title]

        async with httpx.AsyncClient() as client:
            song_url = None
            for query in search_queries:
                response = await client.get(f"{self.base_url}/search", params={"q": query}, headers=self.headers)
                if response.status_code != 200:
                    continue
                
                data = response.json()
                for hit in data["response"]["hits"]:
                    if hit["type"] == "song":
                        # Simple match check
                        hit_artist = hit["result"]["primary_artist"]["name"].lower()
                        if artist.lower() in hit_artist or hit_artist in artist.lower():
                            song_url = hit["result"]["url"]
                            break
                if song_url:
                    break
            
            if not song_url:
                return None

            # Scrape lyrics
            page_response = await client.get(song_url)
            if page_response.status_code != 200:
                return None
            
            soup = BeautifulSoup(page_response.text, "html.parser")
            lyrics_divs = soup.select("[data-lyrics-container='true']")
            
            lyrics = ""
            if lyrics_divs:
                for div in lyrics_divs:
                    lyrics += div.get_text(separator="\n")
            else:
                # Fallback for older pages
                lyrics_div = soup.select_one(".lyrics")
                if lyrics_div:
                    lyrics = lyrics_div.get_text(separator="\n")
            
            return lyrics.strip() if lyrics else None
