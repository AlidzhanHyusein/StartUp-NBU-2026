package com.StartUp.dtos.chat;

import java.time.LocalDateTime;
import java.util.List;

public class ChatDtos {

    public record ChatRequest(
            String message,
            List<MessageHistory> history
    ) {}

    public record MessageHistory(
            String role,
            String content
    ) {}

    public record ChatResponse(
            String reply
    ) {}

    public record ChatHistoryResponse(
            Long id,
            String role,
            String content,
            LocalDateTime createdAt
    ) {}
}