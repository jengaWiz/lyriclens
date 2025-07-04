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
import com.lyriclens.backend.model.SpotifyToken;
import com.lyriclens.backend.repository.SpotifyTokenRepository;
import jakarta.persistence.*;

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

    public SpotifyAuthController(SpotifyTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
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