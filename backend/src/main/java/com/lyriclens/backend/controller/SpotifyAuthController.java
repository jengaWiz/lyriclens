package com.lyriclens.backend.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.util.MultiValueMap;
import org.springframework.util.LinkedMultiValueMap;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;
import com.lyriclens.backend.model.SpotifyToken;
import com.lyriclens.backend.repository.SpotifyTokenRepository;
import jakarta.persistence.*;
import com.lyriclens.backend.service.GeniusLyricsService;
import com.lyriclens.backend.service.EmotionService;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
@RequestMapping("/auth/spotify")
public class SpotifyAuthController {

    @Value("${spotify.client.id}")
    private String clientId;

    @Value("${spotify.client.secret}")
    private String clientSecret;

    @Value("${spotify.redirect.uri}")
    private String redirectUri;

    private final String SPOTIFY_AUTH_URL = "https://accounts.spotify.com/authorize";
    private final String SPOTIFY_TOKEN_URL = "https://accounts.spotify.com/api/token";

    private final SpotifyTokenRepository tokenRepository;
    private final GeniusLyricsService geniusLyricsService;
    private final EmotionService emotionService;

    public SpotifyAuthController(SpotifyTokenRepository tokenRepository, GeniusLyricsService geniusLyricsService, EmotionService emotionService) {
        this.tokenRepository = tokenRepository;
        this.geniusLyricsService = geniusLyricsService;
        this.emotionService = emotionService;
    }

    @GetMapping("/login")
    public ResponseEntity<Void> login() {
        String scope = "user-library-read playlist-read-private playlist-modify-private playlist-modify-public";
        URI uri = UriComponentsBuilder.fromHttpUrl(SPOTIFY_AUTH_URL)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .build().toUri();
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uri);
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/callback")
    public ResponseEntity<Void> callback(@RequestParam("code") String code) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        String auth = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("code", code);
        params.add("redirect_uri", redirectUri);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(SPOTIFY_TOKEN_URL, request, Map.class);
        Map<String, Object> tokenData = response.getBody();
        String accessToken = (String) tokenData.get("access_token");
        String refreshToken = (String) tokenData.get("refresh_token");
        Integer expiresIn = (Integer) tokenData.get("expires_in");

        // Fetch Spotify user ID
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<Map> userResponse = restTemplate.exchange(
            "https://api.spotify.com/v1/me", HttpMethod.GET, userRequest, Map.class);
        Map<String, Object> userData = userResponse.getBody();
        String spotifyUserId = (String) userData.get("id");

        // Store in DB
        SpotifyToken token = tokenRepository.findBySpotifyUserId(spotifyUserId);
        if (token == null) token = new SpotifyToken();
        token.setSpotifyUserId(spotifyUserId);
        token.setAccessToken(accessToken);
        token.setRefreshToken(refreshToken);
        token.setExpiresIn(expiresIn != null ? expiresIn.longValue() : null);
        tokenRepository.save(token);

        // Redirect to frontend dashboard
        HttpHeaders redirectHeaders = new HttpHeaders();
        redirectHeaders.setLocation(URI.create("http://localhost:5173/dashboard?user=" + spotifyUserId));
        return new ResponseEntity<>(redirectHeaders, HttpStatus.FOUND);
    }

    @PostMapping("/create-playlist")
    public ResponseEntity<?> createPlaylist(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        String playlistName = (String) payload.get("playlistName");
        List<String> trackUris = (List<String>) payload.get("trackUris");
        if (userId == null || playlistName == null || trackUris == null || trackUris.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        SpotifyToken token = tokenRepository.findBySpotifyUserId(userId);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String accessToken = token.getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        // 1. Create playlist
        Map<String, Object> playlistBody = new HashMap<>();
        playlistBody.put("name", playlistName);
        playlistBody.put("description", "Created by LyricLens");
        playlistBody.put("public", false);
        HttpEntity<Map<String, Object>> playlistRequest = new HttpEntity<>(playlistBody, headers);
        ResponseEntity<Map> playlistResp = restTemplate.postForEntity(
            "https://api.spotify.com/v1/users/" + userId + "/playlists",
            playlistRequest, Map.class);
        Map<String, Object> playlistData = playlistResp.getBody();
        String playlistId = (String) playlistData.get("id");
        String playlistUrl = (String) playlistData.get("external_urls") != null ?
            ((Map<String, String>)playlistData.get("external_urls")).get("spotify") : null;
        // 2. Add tracks
        Map<String, Object> tracksBody = new HashMap<>();
        tracksBody.put("uris", trackUris);
        HttpEntity<Map<String, Object>> tracksRequest = new HttpEntity<>(tracksBody, headers);
        restTemplate.postForEntity(
            "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks",
            tracksRequest, Map.class);
        // 3. Return playlist URL
        Map<String, Object> result = new HashMap<>();
        result.put("playlistId", playlistId);
        result.put("playlistUrl", playlistUrl);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/analyze-playlists")
    public ResponseEntity<?> analyzePlaylists(@RequestBody Map<String, Object> payload) {
        String userId = (String) payload.get("userId");
        List<?> playlistIdsRaw = (List<?>) payload.get("playlistIds");
        List<String> playlistIds = playlistIdsRaw.stream()
            .map((Object id) -> {
                if (id instanceof String) return (String) id;
                if (id instanceof Map) {
                    Object val = ((Map<?, ?>) id).get("id");
                    return val != null ? val.toString() : "";
                }
                return id != null ? id.toString() : "";
            })
            .filter(id -> !id.isEmpty())
            .collect(Collectors.toList());
        if (userId == null || playlistIds == null || playlistIds.isEmpty()) {
            return ResponseEntity.badRequest().body("Missing required fields");
        }
        SpotifyToken token = tokenRepository.findBySpotifyUserId(userId);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String accessToken = token.getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        Map<String, List<Map<String, Object>>> emotionGroups = new HashMap<>();
        for (String playlistId : playlistIds) {
            // Fetch tracks from playlist
            ResponseEntity<Map> tracksResp = restTemplate.exchange(
                "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks",
                HttpMethod.GET, new HttpEntity<>(headers), Map.class);
            Object itemsObj = tracksResp.getBody().get("items");
            if (!(itemsObj instanceof List)) continue;
            List<?> items = (List<?>) itemsObj;
            for (Object itemObj : items) {
                if (!(itemObj instanceof Map)) {
                    System.out.println("DEBUG: item is not a Map: " + itemObj);
                    continue;
                }
                Map<?, ?> item = (Map<?, ?>) itemObj;
                Object trackObj = item.get("track");
                if (!(trackObj instanceof Map)) {
                    System.out.println("DEBUG: track is not a Map: " + trackObj);
                    continue;
                }
                Map<?, ?> track = (Map<?, ?>) trackObj;

                String trackName = "";
                Object nameObj = track.get("name");
                if (nameObj != null) trackName = nameObj.toString();

                String trackUri = "";
                Object uriObj = track.get("uri");
                if (uriObj != null) trackUri = uriObj.toString();

                String artistName = "";
                Object artistsObj = track.get("artists");
                if (artistsObj instanceof List && !((List<?>) artistsObj).isEmpty()) {
                    Object firstArtist = ((List<?>) artistsObj).get(0);
                    if (firstArtist instanceof Map) {
                        Object artistNameObj = ((Map<?, ?>) firstArtist).get("name");
                        if (artistNameObj != null) artistName = artistNameObj.toString();
                    }
                }
                System.out.println("DEBUG: trackName=" + trackName + ", trackUri=" + trackUri + ", artistName=" + artistName);
                // Fetch lyrics
                String lyrics = geniusLyricsService.fetchLyrics(trackName, artistName);
                // Analyze emotion (blocking for result)
                String emotion = "Unknown";
                if (lyrics != null && !lyrics.trim().isEmpty()) {
                    try {
                        emotion = emotionService.analyzeEmotion(lyrics).block();
                    } catch (Exception e) {
                        // fallback to keyword analysis
                        emotion = geniusLyricsService.analyzeEmotionFromKeywords(trackName, artistName);
                    }
                } else {
                    // Use keyword-based analysis when lyrics are not available
                    emotion = geniusLyricsService.analyzeEmotionFromKeywords(trackName, artistName);
                }
                System.out.println("DEBUG: trackName=" + trackName + ", artistName=" + artistName + ", lyrics=" + lyrics + ", emotion=" + emotion + " (using fallback: " + (lyrics == null || lyrics.trim().isEmpty()) + ")");
                // Group by emotion
                emotionGroups.computeIfAbsent(emotion, k -> new ArrayList<>())
                    .add(Map.of("trackUri", trackUri, "trackName", trackName, "artistName", artistName));
            }
        }
        // Create new playlists for each emotion
        List<Map<String, Object>> createdPlaylists = new ArrayList<>();
        for (Map.Entry<String, List<Map<String, Object>>> entry : emotionGroups.entrySet()) {
            String emotion = entry.getKey();
            List<Map<String, Object>> tracks = entry.getValue();
            if (tracks.isEmpty()) continue;
            // Create playlist
            Map<String, Object> playlistBody = new HashMap<>();
            playlistBody.put("name", emotion + " Anthems");
            playlistBody.put("description", "Created by LyricLens");
            playlistBody.put("public", false);
            HttpEntity<Map<String, Object>> playlistRequest = new HttpEntity<>(playlistBody, headers);
            ResponseEntity<Map> playlistResp = restTemplate.postForEntity(
                "https://api.spotify.com/v1/users/" + userId + "/playlists",
                playlistRequest, Map.class);
            Map<String, Object> playlistData = playlistResp.getBody();
            String playlistId = playlistData.get("id") != null ? playlistData.get("id").toString() : null;
            String playlistUrl = null;
            Object externalUrlsObj = playlistData.get("external_urls");
            if (externalUrlsObj instanceof Map) {
                Object spotifyUrlObj = ((Map<?, ?>) externalUrlsObj).get("spotify");
                if (spotifyUrlObj != null) playlistUrl = spotifyUrlObj.toString();
            }
            // Add tracks
            List<String> uris = new ArrayList<>();
            for (Object t : tracks) {
                if (t instanceof Map) {
                    Object uriObj = ((Map<?, ?>) t).get("trackUri");
                    if (uriObj != null && !uriObj.toString().isEmpty()) uris.add(uriObj.toString());
                } else if (t instanceof String && !((String) t).isEmpty()) {
                    uris.add((String) t);
                }
            }
            if (uris.isEmpty()) continue;
            System.out.println("DEBUG: Adding URIs to playlist: " + uris);
            Map<String, Object> tracksBody = new HashMap<>();
            tracksBody.put("uris", uris);
            HttpEntity<Map<String, Object>> tracksRequest = new HttpEntity<>(tracksBody, headers);
            ResponseEntity<Map> addTracksResp = restTemplate.postForEntity(
                "https://api.spotify.com/v1/playlists/" + playlistId + "/tracks",
                tracksRequest, Map.class);
            System.out.println("DEBUG: Add tracks response: " + addTracksResp.getBody());
            createdPlaylists.add(Map.of(
                "emotion", emotion,
                "playlistId", playlistId,
                "playlistUrl", playlistUrl,
                "trackCount", uris.size()
            ));
        }
        return ResponseEntity.ok(createdPlaylists);
    }

    @GetMapping("/api/spotify/user/{userId}")
    public ResponseEntity<?> getUserProfileAndPlaylists(@PathVariable String userId) {
        SpotifyToken token = tokenRepository.findBySpotifyUserId(userId);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }
        String accessToken = token.getAccessToken();
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        // Fetch user profile
        ResponseEntity<Map> userResp = restTemplate.exchange(
            "https://api.spotify.com/v1/me", HttpMethod.GET, entity, Map.class);
        // Fetch playlists
        ResponseEntity<Map> playlistsResp = restTemplate.exchange(
            "https://api.spotify.com/v1/me/playlists", HttpMethod.GET, entity, Map.class);
        Map<String, Object> result = new HashMap<>();
        result.put("user", userResp.getBody());
        result.put("playlists", playlistsResp.getBody().get("items"));
        return ResponseEntity.ok(result);
    }
} 