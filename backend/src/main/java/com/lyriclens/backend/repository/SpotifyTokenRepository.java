package com.lyriclens.backend.repository;

import com.lyriclens.backend.model.SpotifyToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpotifyTokenRepository extends JpaRepository<SpotifyToken, Long> {
    SpotifyToken findBySpotifyUserId(String spotifyUserId);
} 