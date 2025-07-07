package com.lyriclens.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class GeniusLyricsService {
    private static final Logger logger = LoggerFactory.getLogger(GeniusLyricsService.class);
    
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
            // Check if token is available and valid
            if (geniusToken == null || geniusToken.trim().isEmpty() || 
                geniusToken.equals("YOUR_GENIUS_ACCESS_TOKEN_HERE")) {
                logger.warn("Genius API token is not configured. Skipping lyrics fetch for: {} - {}", title, artist);
                return null;
            }
            logger.debug("Genius token available: {}", geniusToken.substring(0, Math.min(10, geniusToken.length())) + "...");
            
            // Clean the title by removing features and parentheses
            String cleanTitle = cleanSongTitle(title);
            logger.debug("Original title: '{}', Cleaned title: '{}'", title, cleanTitle);
            
            // 1. Search for the song on Genius - try multiple search strategies
            String[] searchQueries = {
                cleanTitle + " " + artist,  // Clean title + artist
                title + " " + artist,       // Original title + artist
                cleanTitle,                 // Just clean title
                title                       // Just original title
            };
            
            String response = null;
            String songUrl = null;
            
            for (String query : searchQueries) {
                if (query.trim().isEmpty()) continue;
                
                logger.debug("Trying search query: '{}'", query);
                
                String searchUrl = UriComponentsBuilder.fromPath("/search")
                        .queryParam("q", query)
                        .build().toString();
                
                response = webClient.get()
                        .uri(searchUrl)
                        .header("Authorization", "Bearer " + geniusToken)
                        .retrieve()
                        .bodyToMono(String.class)
                        .block();
                
                if (response == null) {
                    logger.debug("Genius API returned null response for query: {}", query);
                    continue;
                }
                
                logger.debug("Genius API response length: {} characters", response.length());
                
                // 2. Parse the song URL from the search results
                songUrl = extractSongUrlFromSearch(response, cleanTitle, artist);
                if (songUrl != null) {
                    logger.debug("Found song URL with query '{}': {}", query, songUrl);
                    break;
                }
                
                // If clean title didn't work, try with original title
                if (!query.equals(title)) {
                    songUrl = extractSongUrlFromSearch(response, title, artist);
                    if (songUrl != null) {
                        logger.debug("Found song URL with original title: {}", songUrl);
                        break;
                    }
                }
            }
            
            if (songUrl == null) {
                logger.error("Could not extract song URL from Genius search results for: {} - {}", title, artist);
                return null;
            }
            
            // 3. Scrape the lyrics from the song page
            logger.debug("Attempting to scrape lyrics from: {}", songUrl);
            Document doc = Jsoup.connect(songUrl).get();
            Element lyricsDiv = doc.selectFirst(".lyrics, [data-lyrics-container=true]");
            if (lyricsDiv != null) {
                String lyrics = lyricsDiv.text();
                logger.debug("Found lyrics using primary selector, length: {}", lyrics.length());
                return lyrics;
            }
            
            // Fallback: try to get all text from lyrics containers
            logger.debug("Primary selector failed, trying fallback selectors");
            StringBuilder lyrics = new StringBuilder();
            for (Element el : doc.select("[data-lyrics-container=true]")) {
                lyrics.append(el.text()).append("\n");
            }
            
            if (lyrics.length() > 0) {
                logger.debug("Found lyrics using fallback selector, length: {}", lyrics.length());
                return lyrics.toString().trim();
            }
            
            logger.error("No lyrics found on page: {}", songUrl);
            return null;
            
        } catch (Exception e) {
            logger.error("Exception in fetchLyrics for {} - {}: {}", title, artist, e.getMessage(), e);
            return null;
        }
    }

    /**
     * Clean song title by removing features, parentheses, and other metadata
     */
    private String cleanSongTitle(String title) {
        if (title == null) return "";
        
        String cleaned = title;
        
        // Remove features in parentheses like (feat. Artist) or (with Artist)
        cleaned = cleaned.replaceAll("\\(feat\\.\\s*[^)]*\\)", "");
        cleaned = cleaned.replaceAll("\\(with\\s*[^)]*\\)", "");
        cleaned = cleaned.replaceAll("\\([^)]*feat[^)]*\\)", "");
        
        // Remove other common patterns
        cleaned = cleaned.replaceAll("\\[feat\\.\\s*[^\\]]*\\]", "");
        cleaned = cleaned.replaceAll("\\[with\\s*[^\\]]*\\]", "");
        
        // Remove any remaining parentheses content
        cleaned = cleaned.replaceAll("\\([^)]*\\)", "");
        cleaned = cleaned.replaceAll("\\[[^\\]]*\\]", "");
        
        // Clean up extra whitespace
        cleaned = cleaned.trim().replaceAll("\\s+", " ");
        
        return cleaned;
    }

    /**
     * Fallback emotion analysis based on song title and artist keywords
     * when lyrics are not available
     */
    public String analyzeEmotionFromKeywords(String title, String artist) {
        if (title == null || title.trim().isEmpty()) {
            return "Unknown";
        }
        
        String combinedText = (title + " " + (artist != null ? artist : "")).toLowerCase();
        
        // Clean the text for better keyword matching
        String cleanText = cleanSongTitle(combinedText);
        
        logger.debug("Analyzing emotion from keywords: '{}' (cleaned: '{}')", combinedText, cleanText);
        
        // Happy/Upbeat keywords
        if (cleanText.contains("happy") || cleanText.contains("joy") || 
            cleanText.contains("dance") || cleanText.contains("party") ||
            cleanText.contains("love") || cleanText.contains("beautiful") ||
            cleanText.contains("sunshine") || cleanText.contains("smile") ||
            cleanText.contains("wonderful") || cleanText.contains("amazing") ||
            cleanText.contains("fantastic") || cleanText.contains("great") ||
            cleanText.contains("good") || cleanText.contains("nice") ||
            cleanText.contains("sweet") || cleanText.contains("perfect")) {
            return "Happy";
        }
        
        // Sad/Melancholic keywords
        if (cleanText.contains("sad") || cleanText.contains("cry") || 
            cleanText.contains("tears") || cleanText.contains("lonely") ||
            cleanText.contains("heartbreak") || cleanText.contains("pain") ||
            cleanText.contains("miss") || cleanText.contains("gone") ||
            cleanText.contains("hurt") || cleanText.contains("broken") ||
            cleanText.contains("alone") || cleanText.contains("empty") ||
            cleanText.contains("dark") || cleanText.contains("night") ||
            cleanText.contains("death") || cleanText.contains("die")) {
            return "Sad";
        }
        
        // Energetic/Upbeat keywords
        if (cleanText.contains("energy") || cleanText.contains("fire") || 
            cleanText.contains("power") || cleanText.contains("strong") ||
            cleanText.contains("rock") || cleanText.contains("beat") ||
            cleanText.contains("wild") || cleanText.contains("crazy") ||
            cleanText.contains("trance") || cleanText.contains("dance") ||
            cleanText.contains("party") || cleanText.contains("lit") ||
            cleanText.contains("turnt") || cleanText.contains("hype") ||
            cleanText.contains("banger") || cleanText.contains("bop") ||
            cleanText.contains("vibe") || cleanText.contains("groove")) {
            return "Energetic";
        }
        
        // Calm/Peaceful keywords
        if (cleanText.contains("calm") || cleanText.contains("peace") || 
            cleanText.contains("quiet") || cleanText.contains("gentle") ||
            cleanText.contains("soft") || cleanText.contains("dream") ||
            cleanText.contains("sleep") || cleanText.contains("serene") ||
            cleanText.contains("chill") || cleanText.contains("relax") ||
            cleanText.contains("easy") || cleanText.contains("smooth") ||
            cleanText.contains("mellow") || cleanText.contains("laid") ||
            cleanText.contains("cool") || cleanText.contains("breeze")) {
            return "Calm";
        }
        
        // Romantic keywords
        if (cleanText.contains("romance") || cleanText.contains("kiss") || 
            cleanText.contains("heart") || cleanText.contains("sweet") ||
            cleanText.contains("darling") || cleanText.contains("baby") ||
            cleanText.contains("forever") || cleanText.contains("together") ||
            cleanText.contains("love") || cleanText.contains("lover") ||
            cleanText.contains("romantic") || cleanText.contains("passion") ||
            cleanText.contains("intimate") || cleanText.contains("close")) {
            return "Romantic";
        }
        
        // Aggressive/Angry keywords
        if (cleanText.contains("rage") || cleanText.contains("angry") || 
            cleanText.contains("hate") || cleanText.contains("fight") ||
            cleanText.contains("war") || cleanText.contains("battle") ||
            cleanText.contains("rage") || cleanText.contains("fury") ||
            cleanText.contains("violent") || cleanText.contains("aggressive") ||
            cleanText.contains("hostile") || cleanText.contains("enemy")) {
            return "Aggressive";
        }
        
        // Nostalgic keywords
        if (cleanText.contains("memory") || cleanText.contains("remember") || 
            cleanText.contains("past") || cleanText.contains("old") ||
            cleanText.contains("yesterday") || cleanText.contains("childhood") ||
            cleanText.contains("nostalgia") || cleanText.contains("throwback") ||
            cleanText.contains("retro") || cleanText.contains("vintage")) {
            return "Nostalgic";
        }
        
        // Hopeful/Inspirational keywords
        if (cleanText.contains("hope") || cleanText.contains("dream") || 
            cleanText.contains("future") || cleanText.contains("believe") ||
            cleanText.contains("faith") || cleanText.contains("inspire") ||
            cleanText.contains("motivate") || cleanText.contains("rise") ||
            cleanText.contains("overcome") || cleanText.contains("strength") ||
            cleanText.contains("courage") || cleanText.contains("brave")) {
            return "Hopeful";
        }
        
        // Mysterious/Intriguing keywords
        if (cleanText.contains("mystery") || cleanText.contains("secret") || 
            cleanText.contains("hidden") || cleanText.contains("unknown") ||
            cleanText.contains("strange") || cleanText.contains("weird") ||
            cleanText.contains("curious") || cleanText.contains("magic") ||
            cleanText.contains("spell") || cleanText.contains("enchanted")) {
            return "Mysterious";
        }
        
        // Default to a more neutral category based on common patterns
        if (cleanText.contains("feat") || cleanText.contains("with") || 
            cleanText.contains("remix") || cleanText.contains("mix")) {
            return "Energetic"; // Collaborations often have energy
        }
        
        if (cleanText.contains("trance") || cleanText.contains("dance") || 
            cleanText.contains("club") || cleanText.contains("party")) {
            return "Energetic";
        }
        
        if (cleanText.contains("love") || cleanText.contains("heart") || 
            cleanText.contains("romance")) {
            return "Romantic";
        }
        
        // If we can't determine, return a default based on artist genre hints
        if (artist != null && artist.toLowerCase().contains("travis")) {
            return "Energetic"; // Travis Scott is known for energetic music
        }
        
        return "Unknown";
    }

    private String extractSongUrlFromSearch(String json, String title, String artist) {
        try {
            logger.debug("Extracting song URL from JSON response");
            
            // Simple JSON parsing without external libraries
            String hitsKey = "\"hits\":";
            int hitsIdx = json.indexOf(hitsKey);
            if (hitsIdx == -1) {
                logger.error("Could not find 'hits' key in JSON response");
                return null;
            }
            
            int arrStart = json.indexOf('[', hitsIdx);
            int arrEnd = json.indexOf(']', arrStart);
            if (arrStart == -1 || arrEnd == -1) {
                logger.error("Could not find hits array in JSON response");
                return null;
            }
            
            String hitsArr = json.substring(arrStart, arrEnd);
            logger.debug("Hits array length: {}", hitsArr.length());
            
            String lowerTitle = title.toLowerCase();
            String lowerArtist = artist.toLowerCase();
            
            // Clean the title for better matching
            String cleanLowerTitle = cleanSongTitle(title).toLowerCase();
            
            String[] hits = hitsArr.split("\\{");
            logger.debug("Found {} hits in search results", hits.length);
            
            for (int i = 0; i < hits.length; i++) {
                String hit = hits[i];
                if (hit.trim().isEmpty()) continue;
                
                logger.debug("Checking hit #{}: {}", i, hit.substring(0, Math.min(100, hit.length())));
                
                // Extract song title and artist from the hit
                String hitTitle = extractFieldFromHit(hit, "title");
                String hitArtist = extractFieldFromHit(hit, "primary_artist");
                
                if (hitTitle == null || hitArtist == null) {
                    logger.debug("Hit #{} missing title or artist", i);
                    continue;
                }
                
                String lowerHitTitle = hitTitle.toLowerCase();
                String lowerHitArtist = hitArtist.toLowerCase();
                
                // Clean the hit title for comparison
                String cleanLowerHitTitle = cleanSongTitle(hitTitle).toLowerCase();
                
                // Multiple matching strategies
                boolean titleMatch = lowerHitTitle.contains(lowerTitle) || lowerTitle.contains(lowerHitTitle);
                boolean cleanTitleMatch = cleanLowerHitTitle.contains(cleanLowerTitle) || cleanLowerTitle.contains(cleanLowerHitTitle);
                boolean artistMatch = lowerHitArtist.contains(lowerArtist) || lowerArtist.contains(lowerHitArtist);
                
                // Check if this is a song (not an album or artist page)
                String hitType = extractFieldFromHit(hit, "type");
                boolean isSong = "song".equals(hitType);
                
                logger.debug("Hit #{}: title='{}', artist='{}', type='{}', titleMatch={}, cleanTitleMatch={}, artistMatch={}, isSong={}", 
                           i, hitTitle, hitArtist, hitType, titleMatch, cleanTitleMatch, artistMatch, isSong);
                
                // Score the match quality
                int score = 0;
                if (titleMatch) score += 2;
                if (cleanTitleMatch) score += 3;
                if (artistMatch) score += 2;
                if (isSong) score += 1;
                
                // Require at least a title match and it should be a song
                if ((titleMatch || cleanTitleMatch) && isSong && score >= 3) {
                    logger.debug("Found good match #{} with score {} for {} - {}", i, score, title, artist);
                    
                    int urlIdx = hit.indexOf("\"url\":");
                    if (urlIdx != -1) {
                        int start = hit.indexOf('"', urlIdx + 6) + 1;
                        int end = hit.indexOf('"', start);
                        if (start != -1 && end != -1) {
                            String url = hit.substring(start, end);
                            logger.debug("Extracted URL: {}", url);
                            return url;
                        }
                    }
                }
            }
            
            logger.error("No matching hit found for {} - {}", title, artist);
            return null;
            
        } catch (Exception e) {
            logger.error("Exception in extractSongUrlFromSearch: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Extract a field value from a JSON hit object
     */
    private String extractFieldFromHit(String hit, String fieldName) {
        try {
            String searchKey = "\"" + fieldName + "\":";
            int fieldIdx = hit.indexOf(searchKey);
            if (fieldIdx == -1) return null;
            
            // Handle nested objects (like primary_artist)
            if (fieldName.equals("primary_artist")) {
                // Look for the name field within the artist object
                int nameIdx = hit.indexOf("\"name\":", fieldIdx);
                if (nameIdx == -1) return null;
                
                int start = hit.indexOf('"', nameIdx + 7) + 1;
                int end = hit.indexOf('"', start);
                if (start != -1 && end != -1) {
                    return hit.substring(start, end);
                }
            } else {
                // Simple string field
                int start = hit.indexOf('"', fieldIdx + searchKey.length()) + 1;
                int end = hit.indexOf('"', start);
                if (start != -1 && end != -1) {
                    return hit.substring(start, end);
                }
            }
            
            return null;
        } catch (Exception e) {
            logger.debug("Error extracting field '{}' from hit: {}", fieldName, e.getMessage());
            return null;
        }
    }
} 