package com.lyriclens.backend.controller;

import com.lyriclens.backend.model.LyricsHistory;
import com.lyriclens.backend.model.LyricsRequest;
import com.lyriclens.backend.service.EmotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import reactor.core.publisher.Mono;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/lyrics")
public class LyricsController {

    private final EmotionService emotionService;

    public LyricsController(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    @PostMapping("/analyze")
    public Mono<ResponseEntity<Map<String, String>>> analyzeLyrics(@RequestBody Map<String, Object> payload) {
        String lyrics = (String) payload.get("lyrics");
        String song = (String) payload.get("song");
        String artist = (String) payload.get("artist");
        return emotionService.analyzeEmotion(lyrics)
            .map(emotionCategory -> {
                String explanation = "The lyrics express " + emotionCategory + " emotion.";
                Map<String, String> response = new HashMap<>();
                response.put("song", song != null ? song : "");
                response.put("artist", artist != null ? artist : "");
                response.put("emotionCategory", capitalize(emotionCategory));
                response.put("explanation", explanation);
                return ResponseEntity.ok(response);
            });
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }

    @GetMapping("/history")
    public ResponseEntity<List<LyricsHistory>> getHistory() {
        List<LyricsHistory> historyList = emotionService.getLyricsHistory();
        return ResponseEntity.ok(historyList);
    }

}
