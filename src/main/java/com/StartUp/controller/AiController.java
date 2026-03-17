package com.StartUp.controller;

import com.StartUp.dtos.chat.ChatDtos;
import com.StartUp.service.AiService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @Operation(summary = "Chat with Gemini AI assistant")
    @PostMapping("/chat")
    public ResponseEntity<ChatDtos.ChatResponse> chat(@RequestBody ChatDtos.ChatRequest request) {
        return ResponseEntity.ok(aiService.chat(request));
    }
}