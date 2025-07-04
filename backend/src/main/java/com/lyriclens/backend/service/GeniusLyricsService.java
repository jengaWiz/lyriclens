package com.lyriclens.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

@Service
public class GeniusLyricsService {
    @Value("${genius.api.token}")
    private String geniusToken;

    private final WebClient webClient;

    public GeniusLyricsService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://api.genius.com")
                .build();
    }

    public String fetchLyrics(String title, String artist) {
        try {
            // 1. Search for the song on Genius
            String query = title + " " + artist;
            String searchUrl = UriComponentsBuilder.fromPath("/search")
                    .queryParam("q", query)
                    .build().toString();
            String response = webClient.get()
                    .uri(searchUrl)
                    .header("Authorization", "Bearer " + geniusToken)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
            if (response == null) return null;
            // 2. Parse the song URL from the search results
            String songUrl = extractSongUrlFromSearch(response, title, artist);
            if (songUrl == null) return null;
            // 3. Scrape the lyrics from the song page
            Document doc = Jsoup.connect(songUrl).get();
            Element lyricsDiv = doc.selectFirst(".lyrics, [data-lyrics-container=true]");
            if (lyricsDiv != null) {
                return lyricsDiv.text();
            }
            // Fallback: try to get all text from lyrics containers
            StringBuilder lyrics = new StringBuilder();
            for (Element el : doc.select("[data-lyrics-container=true]")) {
                lyrics.append(el.text()).append("\n");
            }
            return lyrics.length() > 0 ? lyrics.toString().trim() : null;
        } catch (Exception e) {
            return null;
        }
    }

    private String extractSongUrlFromSearch(String json, String title, String artist) {
        try {
            // Simple JSON parsing without external libraries
            String hitsKey = "\"hits\":";
            int hitsIdx = json.indexOf(hitsKey);
            if (hitsIdx == -1) return null;
            int arrStart = json.indexOf('[', hitsIdx);
            int arrEnd = json.indexOf(']', arrStart);
            if (arrStart == -1 || arrEnd == -1) return null;
            String hitsArr = json.substring(arrStart, arrEnd);
            String lowerTitle = title.toLowerCase();
            String lowerArtist = artist.toLowerCase();
            for (String hit : hitsArr.split("\{")) {
                if (hit.toLowerCase().contains(lowerTitle) && hit.toLowerCase().contains(lowerArtist)) {
                    int urlIdx = hit.indexOf("\"url\":");
                    if (urlIdx != -1) {
                        int start = hit.indexOf('"', urlIdx + 6) + 1;
                        int end = hit.indexOf('"', start);
                        if (start != -1 && end != -1) {
                            return hit.substring(start, end);
                        }
                    }
                }
            }
            return null;
        } catch (Exception e) {
            return null;
        }
    }
} 