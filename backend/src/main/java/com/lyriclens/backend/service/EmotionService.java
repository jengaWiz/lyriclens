package com.lyriclens.backend.service;

import org.springframework.stereotype.Service;

@Service
public class EmotionService {

    public String analyzeEmotion(String lyrics) {
        if(lyrics.toLowerCase().contains("love")){
            return "happy";
        } else
            return "neutral";
    }

}
