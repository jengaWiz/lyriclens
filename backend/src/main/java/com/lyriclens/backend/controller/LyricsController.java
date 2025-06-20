package com.lyriclens.backend.controller;

import com.lyriclens.backend.model.LyricsHistory;
import com.lyriclens.backend.model.LyricsRequest;
import com.lyriclens.backend.service.EmotionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/api/lyrics")
public class LyricsController {

    private final EmotionService emotionService;

    public LyricsController(EmotionService emotionService) {
        this.emotionService = emotionService;
    }

    @PostMapping("/analyze")
    public ResponseEntity<Map<String, String>> analyzeLyrics(@RequestBody LyricsRequest request) {
        String emotion = emotionService.analyzeEmotion(request.getLyrics());

        Map<String, String> response = new HashMap<>();
        response.put("emotion", emotion);

        return ResponseEntity.ok(response);
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
