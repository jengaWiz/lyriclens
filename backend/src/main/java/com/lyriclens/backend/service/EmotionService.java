package com.lyriclens.backend.service;

import com.lyriclens.backend.model.LyricsHistory;
import com.lyriclens.backend.repository.LyricsHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmotionService {

    private final LyricsHistoryRepository historyRepository;

    public EmotionService(LyricsHistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public String analyzeEmotion(String lyrics) {
        try{
            // Map keywords to PRD subcategories
            String lowerLyrics = lyrics.toLowerCase();
            if (lowerLyrics.contains("sad") || lowerLyrics.contains("cry") || lowerLyrics.contains("breakup") || lowerLyrics.contains("lost love")) {
                return "Heartbreak";
            } else if (lowerLyrics.contains("memory") || lowerLyrics.contains("remember") || lowerLyrics.contains("past") || lowerLyrics.contains("old times")) {
                return "Nostalgia";
            } else if (lowerLyrics.contains("strong") || lowerLyrics.contains("rise") || lowerLyrics.contains("power") || lowerLyrics.contains("fight") || lowerLyrics.contains("win") || lowerLyrics.contains("empower")) {
                return "Empowerment";
            } else if (lowerLyrics.contains("betray") || lowerLyrics.contains("deceive") || lowerLyrics.contains("lie") || lowerLyrics.contains("cheat")) {
                return "Betrayal";
            } else if (lowerLyrics.contains("party") || lowerLyrics.contains("celebrate") || lowerLyrics.contains("joy") || lowerLyrics.contains("dance") || lowerLyrics.contains("happy")) {
                return "Celebration";
            }
            // Default fallback
            return "Unknown";
        } catch (Exception e) {
            return "Unknown";
        }
    }

    public List<LyricsHistory> getLyricsHistory() {
        return historyRepository.findAll();
    }


}
