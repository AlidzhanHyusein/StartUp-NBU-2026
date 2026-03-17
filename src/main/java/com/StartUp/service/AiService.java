package com.StartUp.service;

import com.StartUp.dtos.chat.ChatDtos;
import com.StartUp.entity.ChatMessage;
import com.StartUp.entity.User;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.ChatMessageRepository;
import com.StartUp.repository.UserRepository;
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

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

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

    public ChatDtos.ChatResponse chat(String email, ChatDtos.ChatRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        List<Map<String, Object>> contents = new ArrayList<>();

        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", SYSTEM_PROMPT))));
        contents.add(Map.of("role", "model", "parts", List.of(Map.of("text", "Understood! I am the Breaddy assistant. How can I help you?"))));

        List<ChatMessage> dbHistory = chatMessageRepository.findByUserIdOrderByCreatedAtAsc(user.getId());
        for (ChatMessage msg : dbHistory) {
            contents.add(Map.of(
                    "role", msg.getRole(),
                    "parts", List.of(Map.of("text", msg.getContent()))
            ));
        }

        contents.add(Map.of("role", "user", "parts", List.of(Map.of("text", request.message()))));

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

        chatMessageRepository.save(ChatMessage.builder()
                .user(user)
                .role("user")
                .content(request.message())
                .build());

        chatMessageRepository.save(ChatMessage.builder()
                .user(user)
                .role("model")
                .content(reply)
                .build());

        return new ChatDtos.ChatResponse(reply);
    }

    public List<ChatDtos.ChatHistoryResponse> getHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));

        return chatMessageRepository.findByUserIdOrderByCreatedAtAsc(user.getId())
                .stream()
                .map(msg -> new ChatDtos.ChatHistoryResponse(
                        msg.getId(),
                        msg.getRole(),
                        msg.getContent(),
                        msg.getCreatedAt()
                ))
                .toList();
    }

    public void clearHistory(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));
        chatMessageRepository.deleteByUserId(user.getId());
    }
}