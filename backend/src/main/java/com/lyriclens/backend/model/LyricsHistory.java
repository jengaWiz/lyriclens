package com.lyriclens.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class LyricsHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String lyrics;
    private String emotion;
    private LocalDateTime timestamp = LocalDateTime.now();

    public LyricsHistory() {
    }

    public LyricsHistory(String lyrics, String emotion){
        this.lyrics = lyrics;
        this.emotion = emotion;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getEmotion() {
        return emotion;
    }

    public void setEmotion(String emotion) {
        this.emotion = emotion;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

