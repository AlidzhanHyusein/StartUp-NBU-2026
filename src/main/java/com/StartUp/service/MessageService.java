package com.StartUp.service;

import com.StartUp.entity.Conversation;
import com.StartUp.entity.Message;
import com.StartUp.entity.Notification;
import com.StartUp.entity.User;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.ConversationRepository;
import com.StartUp.repository.MessageRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

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


    @Transactional
    public Message sendMessage(Conversation conversation, User sender, String content) {
        Message message = Message.builder()
                .conversation(conversation)
                .sender(sender)
                .content(content)
                .isRead(false)
                .build();

        Message savedMessage = messageRepository.save(message);

        conversation.setLastMessageAt(LocalDateTime.now());
        conversationRepository.save(conversation);

        User receiver = conversation.getUser1().getId().equals(sender.getId())
                ? conversation.getUser2() 
                : conversation.getUser1();

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

    @Transactional
    public Conversation getOrCreateConversation(Long user1Id, Long user2Id) {
        Optional<Conversation> existing = conversationRepository
                .findConversationBetween(user1Id, user2Id);

        if (existing.isPresent()) {
            return existing.get();
        }

        User u1 = userRepository.findById(user1Id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found: " + user1Id));
        User u2 = userRepository.findById(user2Id)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found: " + user2Id));

        Conversation c = new Conversation();
        c.setUser1(u1);
        c.setUser2(u2);
        return conversationRepository.save(c);
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

    public List<Conversation> getMyConversations(Long userId) {
        return conversationRepository.findByUser1IdOrUser2IdOrderByLastMessageAtDesc(userId, userId);
    }

    public List<Message> getConversationMessages(Long conversationId) {
        return messageRepository.findByConversationIdOrderBySentAtAsc(conversationId);
    }

    @Transactional
    public void markConversationAsRead(Long conversationId, Long userId) {
        messageRepository.markAllAsReadInConversation(conversationId, userId);
    }

    public Long getUnreadCount(Long conversationId, Long userId) {
        return messageRepository.countUnreadMessages(conversationId, userId);
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
