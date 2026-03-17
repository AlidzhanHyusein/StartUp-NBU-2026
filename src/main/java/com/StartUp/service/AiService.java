package com.StartUp.service;

import com.StartUp.dtos.chat.ChatDtos;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.*;

@Service
@RequiredArgsConstructor
public class AiService {

    @Value("${gemini.api-key}")
    private String apiKey;

    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://generativelanguage.googleapis.com")
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .build();

    private static final String SYSTEM_PROMPT = """
            You are a helpful assistant for Breaddy — a platform that connects students with part-time jobs and gigs.
            
            About Breaddy:
            - Students can register, create profiles, upload CVs and apply for jobs
            - Employers can post jobs, review applications and hire students
            - Job types: Part-time, Full-time, One-day gigs
            - Categories: Restaurant, Shop, Event, Logistics, Promotion
            - Students can message employers and receive notifications
            - Payments are handled through the platform
            
            Your role:
            - Help users navigate the platform
            - Answer questions about jobs, applications, profiles
            - Guide students on how to apply for jobs
            - Guide employers on how to post jobs
            - Be friendly, concise and helpful
            - If you don't know something specific, suggest contacting support
            
            Always respond in the same language the user is writing in.
            """;

    public ChatDtos.ChatResponse chat(ChatDtos.ChatRequest request) {
        List<Map<String, Object>> contents = new ArrayList<>();

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", SYSTEM_PROMPT))
        ));
        contents.add(Map.of(
                "role", "model",
                "parts", List.of(Map.of("text", "Understood! I am the Breaddy assistant. How can I help you?"))
        ));

        if (request.history() != null) {
            for (ChatDtos.MessageHistory msg : request.history()) {
                contents.add(Map.of(
                        "role", msg.role(),
                        "parts", List.of(Map.of("text", msg.content()))
                ));
            }
        }

        contents.add(Map.of(
                "role", "user",
                "parts", List.of(Map.of("text", request.message()))
        ));

        Map<String, Object> body = Map.of("contents", contents);

        Map response = webClient.post()
                .uri("/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        List<Map<String, Object>> candidates = (List<Map<String, Object>>) response.get("candidates");
        Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
        List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
        String reply = (String) parts.get(0).get("text");

        return new ChatDtos.ChatResponse(reply);
    }
}