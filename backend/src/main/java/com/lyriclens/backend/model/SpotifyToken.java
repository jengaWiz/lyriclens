package com.lyriclens.backend.model;

import jakarta.persistence.*;

@Entity
public class SpotifyToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String spotifyUserId;

    @Column(length = 2048)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;
    private Long expiresIn;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getSpotifyUserId() { return spotifyUserId; }
    public void setSpotifyUserId(String spotifyUserId) { this.spotifyUserId = spotifyUserId; }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
} 