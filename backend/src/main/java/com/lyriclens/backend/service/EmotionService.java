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
            // 🔁 Dummy emotion logic
            String emotion = "neutral";
            String lowerLyrics = lyrics.toLowerCase();

            if (lowerLyrics.contains("happy") || lowerLyrics.contains("love")) {
                emotion = "happy";
            } else if (lowerLyrics.contains("sad") || lowerLyrics.contains("cry")) {
                emotion = "sad";
            } else if (lowerLyrics.contains("angry") || lowerLyrics.contains("hate")) {
                emotion = "angry";
            }

            LyricsHistory history = new LyricsHistory();
            history.setLyrics(lyrics);
            history.setEmotion(emotion);
            history.setTimestamp(LocalDateTime.now());
            historyRepository.save(history);

            return emotion;

        } catch (Exception e) {
            return "Error analyzing emotion";
        }
    }

    public List<LyricsHistory> getLyricsHistory() {
        return historyRepository.findAll();
    }


}
