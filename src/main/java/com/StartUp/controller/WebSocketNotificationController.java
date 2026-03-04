package com.StartUp.controller;

import com.StartUp.entity.Notification;
import com.StartUp.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class WebSocketNotificationController {

    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/notifications.markRead")
    public void markNotificationAsRead(@Payload Map<String, Object> payload) {
        Long notificationId = Long.valueOf(payload.get("notificationId").toString());
        Long userId = Long.valueOf(payload.get("userId").toString());

        Notification notification = notificationService.markAsRead(notificationId);

        // Изпращане на потвърждение за прочетено
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications/read",
                Map.of("notificationId", notificationId, "readAt", notification.getReadAt())
        );
    }

    @MessageMapping("/notifications.markAllRead")
    public void markAllNotificationsAsRead(@Payload Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());

        notificationService.markAllAsRead(userId);

        // Изпращане на потвърждение
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications/allRead",
                Map.of("success", true)
        );
    }

    @MessageMapping("/notifications.getUnread")
    public void getUnreadNotifications(@Payload Map<String, Object> payload) {
        Long userId = Long.valueOf(payload.get("userId").toString());

        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(userId);

        // Изпращане на непрочетените нотификации
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notifications/unread",
                unreadNotifications
        );
    }
}
