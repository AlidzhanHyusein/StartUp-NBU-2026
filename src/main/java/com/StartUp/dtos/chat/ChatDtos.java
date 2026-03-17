package com.StartUp.dtos.chat;

import java.util.List;

public class ChatDtos {

    public record ChatRequest(
            String message,
            List<MessageHistory> history
    ) {}

    public record MessageHistory(
            String role,  // "user" or "model"
            String content
    ) {}

    public record ChatResponse(
            String reply
    ) {}
}