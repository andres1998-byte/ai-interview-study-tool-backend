package com.andres.ai_study_tool.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class LlmClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.api.key}")
    private String apiKey;

    @Value("${llm.model}")
    private String model;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String generate(String systemPrompt, String userPrompt) {

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", new Object[] {
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                },
                "temperature", 0.2
        );

        var headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + apiKey);
        headers.set("Content-Type", "application/json");

        var entity = new org.springframework.http.HttpEntity<>(requestBody, headers);

        var response = restTemplate.postForObject(
                OPENAI_URL,
                entity,
                Map.class
        );

        // Extract text safely
        var choices = (java.util.List<Map<String, Object>>) response.get("choices");
        var message = (Map<String, Object>) choices.get(0).get("message");

        return message.get("content").toString();
    }
}
