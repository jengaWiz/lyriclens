package com.lyriclens.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final WebClient webClient;

    public GeminiService() {
        this.webClient = WebClient.builder()
                .baseUrl("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateContent")
                .build();
    }

    public Mono<String> classifyEmotion(String lyrics) {
        String prompt = "Classify the dominant emotional subcategory in the following song lyrics. Choose **only one** from this list: "
                + "[heartbreak, betrayal, longing, nostalgia, celebration, confidence, motivation, loneliness, grief, euphoria, rebellion, hope, friendship, guilt, shame, rage, serenity, anxiety, confusion, wonder]. "
                + "Respond only with the emotion, nothing else. Lyrics: " + lyrics;

        return webClient.post()
                .uri("?key=" + apiKey)
                .bodyValue("{\"contents\": [{\"parts\": [{\"text\": \"" + prompt + "\"}]}]}")
                .header("Content-Type", "application/json")
                .retrieve()
                .bodyToMono(String.class);  // We'll extract just the text from the JSON later
    }
}
