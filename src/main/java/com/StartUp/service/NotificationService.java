package com.StartUp.service;

import com.StartUp.entity.Notification;
import com.StartUp.entity.User;
import com.StartUp.exception.AppExceptions;
import com.StartUp.repository.NotificationRepository;
import com.StartUp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserRepository userRepository;

    @Transactional
    public Notification createNotification(User user, Notification.NotificationType type, 
                                          String title, String message, 
                                          Long relatedEntityId, String relatedEntityType) {
        Notification notification = Notification.builder()
                .user(user)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .relatedEntityId(relatedEntityId)
                .relatedEntityType(relatedEntityType)
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        messagingTemplate.convertAndSendToUser(
                user.getId().toString(),
                "/queue/notifications",
                savedNotification
        );

        return savedNotification;
    }

    @Transactional
    public void sendNotification(User recipient, String title, String message,
                                 String typeString, Long referenceId) {
        Notification.NotificationType type;
        try {
            type = Notification.NotificationType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            type = Notification.NotificationType.SYSTEM;
        }

        Notification notification = Notification.builder()
                .user(recipient)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .relatedEntityId(referenceId)
                .relatedEntityType("GROUP_BOOKING")
                .build();

        notificationRepository.save(notification);
    }
    @Transactional
    public void markAllReadByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppExceptions.ResourceNotFoundException("User not found"));
        notificationRepository.markAllReadByUserId(user.getId());
    }

    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public Long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setIsRead(true);
        notification.setReadAt(LocalDateTime.now());

        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        
        unreadNotifications.forEach(notification -> {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        });

        notificationRepository.saveAll(unreadNotifications);
    }

    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }

    public Notification getNotificationById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
    }
}
