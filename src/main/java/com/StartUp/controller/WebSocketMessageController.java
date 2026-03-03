package com.StartUp.controller;

import com.StartUp.entity.Conversation;
import com.StartUp.entity.Message;
import com.StartUp.entity.User;
import com.StartUp.service.MessageService;
import com.StartUp.repository.ConversationRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketMessageController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConversationRepository conversationRepository;
    private final UserRepository userRepository;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload Map<String, Object> payload) {
        Long conversationId = Long.valueOf(payload.get("conversationId").toString());
        Long senderId = Long.valueOf(payload.get("senderId").toString());
        String content = payload.get("content").toString();

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
        
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Message message = messageService.sendMessage(conversation, sender, content);

        // Изпращане на съобщението в реално време към всички участници в разговора
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId,
                message
        );
    }

    @MessageMapping("/chat.typing")
    public void userTyping(@Payload Map<String, Object> payload) {
        Long conversationId = Long.valueOf(payload.get("conversationId").toString());
        Long userId = Long.valueOf(payload.get("userId").toString());
        Boolean isTyping = (Boolean) payload.get("isTyping");

        // Изпращане на typing indicator
        String destination = "/topic/conversation/" + conversationId + "/typing";
        Map<String, Object> payload1 = Map.of("userId", userId, "isTyping", isTyping);
        messagingTemplate.convertAndSend(destination, (Object) payload1);
    }

    @MessageMapping("/chat.read")
    public void markAsRead(@Payload Map<String, Object> payload) {
        Long messageId = Long.valueOf(payload.get("messageId").toString());
        Long conversationId = Long.valueOf(payload.get("conversationId").toString());

        Message message = messageService.markAsRead(messageId);

        // Нотифициране че съобщението е прочетено
        String destination = "/topic/conversation/" + conversationId + "/read";
        Map<String, Object> payload2 = Map.of("messageId", messageId, "readAt", message.getSentAt());
        messagingTemplate.convertAndSend(destination, (Object) payload2);
    }
}
