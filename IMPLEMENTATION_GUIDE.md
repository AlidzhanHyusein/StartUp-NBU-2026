# B3 - Payments + Reviews + Messages + Notifications

## REST API

### Payments `/api/payments`
```
POST /api/payments/calculate
POST /api/payments/{id}/confirm
GET  /api/payments/history/{userId}
GET  /api/payments/status/{status}
```

### Reviews `/api/reviews`
```
GET    /api/reviews/user/{userId}
GET    /api/reviews/user/{userId}/average
PUT    /api/reviews/{id}
DELETE /api/reviews/{id}
```

### Messages `/api/messages`
```
GET    /api/messages/conversations
GET    /api/messages/conversations/{id}
GET    /api/messages
PUT    /api/messages/{id}/read
DELETE /api/messages/{id}
```

### Notifications `/api/notifications`
```
GET /api/notifications/user/{userId}/unread
GET /api/notifications/user/{userId}/unread/count
PUT /api/notifications/{id}/read
PUT /api/notifications/user/{userId}/read-all
```

## WebSocket

**Connection:** `ws://localhost:8080/ws`

**Messages:**
```
Send: /app/chat.send → Subscribe: /topic/conversation/{id}
Send: /app/chat.typing → Subscribe: /topic/conversation/{id}/typing
Send: /app/chat.read → Subscribe: /topic/conversation/{id}/read
```

**Notifications:**
```
Auto: /user/{userId}/queue/notifications
Send: /app/notifications.markRead
Send: /app/notifications.markAllRead
```

## Business Logic

- **Payment:** 10% комисионна, auto нотификации
- **Review:** Auto update на User.rating (AVG)
- **Message:** Auto lastMessageAt, WebSocket real-time
- **Notification:** Auto при events (одобрение, плащане, съобщение, ревю)
