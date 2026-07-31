package com.documind.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * The "G" in RAG. Sends the user question plus retrieved context to Groq's
 * OpenAI-compatible chat completions endpoint and returns the generated answer.
 */
@Service
public class GroqService {

    private final RestClient restClient = RestClient.create();

    @Value("${groq.api.key}")
    private String apiKey;

    @Value("${groq.model:llama-3.1-8b-instant}")
    private String model;

    private static final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public String generateAnswer(String question, String context) {
        String systemPrompt = """
                You are DocuMind, a precise assistant that answers ONLY using the
                provided context from the user's uploaded documents.
                If the context does not contain the answer, say so clearly instead
                of guessing. Keep answers concise and cite chunk numbers like [1], [2]
                when you use them.
                """;

        String userPrompt = "Context:\n" + context + "\n\nQuestion: " + question;

        Map<String, Object> body = Map.of(
                "model", model,
                "temperature", 0.2,
                "messages", List.of(
                        Map.of("role", "system", "content", systemPrompt),
                        Map.of("role", "user", "content", userPrompt)
                )
        );



        Map<?, ?> response = restClient
                .method(HttpMethod.POST)
                .uri(GROQ_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

        if (response == null || !response.containsKey("choices")) {
            throw new IllegalStateException("Groq API returned no choices - check your API key/model.");
        }

        List<?> choices = (List<?>) response.get("choices");
        Map<?, ?> first = (Map<?, ?>) choices.get(0);
        Map<?, ?> message = (Map<?, ?>) first.get("message");
        return String.valueOf(message.get("content"));
    }
}
