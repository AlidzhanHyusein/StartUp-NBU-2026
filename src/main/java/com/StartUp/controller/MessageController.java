package com.StartUp.controller;

import com.StartUp.dtos.ConversationRequest;
import com.StartUp.dtos.MessageRequest;
import com.StartUp.entity.Conversation;
import com.StartUp.entity.Message;
import com.StartUp.entity.User;
import com.StartUp.repository.UserRepository;
import com.StartUp.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    private final UserRepository userRepository;

    @GetMapping("/conversations")
    public ResponseEntity<List<Conversation>> getAllConversations() {
        List<Conversation> conversations = messageService.getAllConversations();
        return ResponseEntity.ok(conversations);
    }

    @GetMapping("/conversations/{id}")
    public ResponseEntity<Conversation> getConversationById(@PathVariable Long id) {
        Conversation conversation = messageService.getConversationById(id);
        return ResponseEntity.ok(conversation);
    }

    @GetMapping("/conversations/{id}/messages")
    public ResponseEntity<List<Message>> getConversationMessages(@PathVariable Long id) {
        return ResponseEntity.ok(messageService.getConversationMessages(id));
    }

    @PutMapping("/conversations/{id}/read")
    public ResponseEntity<Void> markConversationAsRead(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        messageService.markConversationAsRead(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations/{id}/unread")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(Map.of("unread", messageService.getUnreadCount(id, user.getId())));
    }

    @DeleteMapping("/conversations/{id}")
    public ResponseEntity<Void> deleteConversation(@PathVariable Long id) {
        messageService.deleteConversation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/conversations/my")
    public ResponseEntity<List<Conversation>> getMyConversations(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        return ResponseEntity.ok(messageService.getMyConversations(user.getId()));
    }



    @GetMapping
    public ResponseEntity<List<Message>> getAllMessages() {
        List<Message> messages = messageService.getAllMessages();
        return ResponseEntity.ok(messages);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Message> getMessageById(@PathVariable Long id) {
        Message message = messageService.getMessageById(id);
        return ResponseEntity.ok(message);
    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<Message> markAsRead(@PathVariable Long messageId) {
        Message message = messageService.markAsRead(messageId);
        return ResponseEntity.ok(message);
    }

    @PostMapping("/conversations")
    public ResponseEntity<Conversation> createConversation(@RequestBody ConversationRequest request) {
        User user1 = userRepository.findById(request.getUser1Id()).orElseThrow();
        User user2 = userRepository.findById(request.getUser2Id()).orElseThrow();
        return ResponseEntity.ok(messageService.createConversation(user1, user2));
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(@RequestBody MessageRequest request) {
        Conversation conversation = messageService.getConversationById(request.getConversationId());
        User sender = userRepository.findById(request.getSenderId()).orElseThrow();
        return ResponseEntity.ok(messageService.sendMessage(conversation, sender, request.getContent()));
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Message> updateMessage(
            @PathVariable Long messageId,
            @RequestBody Map<String, String> request) {
        String newContent = request.get("content");
        Message message = messageService.updateMessage(messageId, newContent);
        return ResponseEntity.ok(message);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMessage(@PathVariable Long id) {
        messageService.deleteMessage(id);
        return ResponseEntity.noContent().build();
    }
}
