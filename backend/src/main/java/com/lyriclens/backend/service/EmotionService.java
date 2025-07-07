package com.lyriclens.backend.service;

import com.lyriclens.backend.model.LyricsHistory;
import com.lyriclens.backend.repository.LyricsHistoryRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import reactor.core.publisher.Mono;
import com.lyriclens.backend.service.GeminiService;

@Service
public class EmotionService {

    private final LyricsHistoryRepository historyRepository;
    private final GeminiService geminiService;

    public EmotionService(LyricsHistoryRepository historyRepository, GeminiService geminiService) {
        this.historyRepository = historyRepository;
        this.geminiService = geminiService;
    }

    public Mono<String> analyzeEmotion(String lyrics) {
        return geminiService.classifyEmotion(lyrics)
            .map(response -> {
                // Extract the emotion from Gemini's JSON response
                // (Assume response is a JSON string with the emotion in a known field, or just the emotion as plain text)
                // You may need to parse the response if Gemini returns more than just the emotion
                // For now, return the raw response
                return response.replaceAll("[\"{}]", "").trim();
            });
    }

    public List<LyricsHistory> getLyricsHistory() {
        return historyRepository.findAll();
    }


}
