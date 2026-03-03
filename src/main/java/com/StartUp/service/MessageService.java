package com.StartUp.service;

import com.StartUp.entity.Conversation;
import com.StartUp.entity.Message;
import com.StartUp.entity.Notification;
import com.StartUp.entity.User;
import com.StartUp.repository.ConversationRepository;
import com.StartUp.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationService notificationService;

    // Conversation CRUD
    
    @Transactional
    public Conversation createConversation(User user1, User user2) {
        Conversation conversation = Conversation.builder()
                .user1(user1)
                .user2(user2)
                .build();
        
        return conversationRepository.save(conversation);
    }

    public Conversation getConversationById(Long id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation not found"));
    }

    public List<Conversation> getAllConversations() {
        return conversationRepository.findAll();
    }

    @Transactional
    public void deleteConversation(Long id) {
        conversationRepository.deleteById(id);
    }

    // Message CRUD

    @Transactional
    public Message sendMessage(Conversation conversation, User sender, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        // Обновяване на lastMessageAt на разговора
        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        // Определяне на получателя
        User receiver = conversation.getUser1().getId().equals(sender.getId()) 
                ? conversation.getUser2() 
                : conversation.getUser1();

        // Нотификация за нов месидж
        notificationService.createNotification(
                receiver,
                Notification.NotificationType.NEW_MESSAGE,
                "Ново съобщение",
                sender.getFullName() + " ви изпрати съобщение",
                savedMessage.getId(),
                "Message"
        );

        return savedMessage;
    }

    public Message getMessageById(Long id) {
        return messageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Message not found"));
    }

    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Transactional
    public Message markAsRead(Long messageId) {
        Message message = getMessageById(messageId);
        message.setIsRead(true);
        return messageRepository.save(message);
    }

    @Transactional
    public void deleteMessage(Long id) {
        messageRepository.deleteById(id);
    }

    @Transactional
    public Message updateMessage(Long messageId, String newContent) {
        Message message = getMessageById(messageId);
        message.setContent(newContent);
        return messageRepository.save(message);
    }
}
