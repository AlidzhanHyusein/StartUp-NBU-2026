package com.StartUp.controller;

import com.StartUp.dtos.chat.ChatDtos;
import com.StartUp.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "Chat with Gemini AI assistant")
    @PostMapping("/chat")
    public ResponseEntity<ChatDtos.ChatResponse> chat(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody ChatDtos.ChatRequest request) {
        return ResponseEntity.ok(aiService.chat(userDetails.getUsername(), request));
    }

    @Operation(summary = "Get chat history")
    @GetMapping("/history")
    public ResponseEntity<List<ChatDtos.ChatHistoryResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(aiService.getHistory(userDetails.getUsername()));
    }

    @Operation(summary = "Clear chat history")
    @DeleteMapping("/history")
    public ResponseEntity<Void> clearHistory(
            @AuthenticationPrincipal UserDetails userDetails) {
        aiService.clearHistory(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }
}