package com.scamshield.notifications;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "notifications")
@CompoundIndex(name = "notification_owner_time_idx", def = "{'userId': 1, 'createdAt': -1}")
@CompoundIndex(name = "notification_event_dedupe_idx", def = "{'userId': 1, 'eventKey': 1}", unique = true)
public class Notification {
    @Id
    private String id;
    private String userId;
    private String title;
    private String message;
    private NotificationType type;
    private NotificationPriority priority;
    private boolean read;
    private String actionUrl;
    private String eventKey;
    private Instant createdAt;
    private Instant readAt;

    protected Notification() {
    }

    public Notification(
            String userId,
            String title,
            String message,
            NotificationType type,
            NotificationPriority priority,
            String actionUrl,
            String eventKey,
            Instant createdAt) {
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.priority = priority;
        this.actionUrl = actionUrl;
        this.eventKey = eventKey;
        this.createdAt = createdAt;
        this.read = false;
    }

    public void markRead(Instant at) { read = true; readAt = at; }
    public void markUnread() { read = false; readAt = null; }
    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public NotificationType getType() { return type; }
    public NotificationPriority getPriority() { return priority; }
    public boolean isRead() { return read; }
    public String getActionUrl() { return actionUrl; }
    public String getEventKey() { return eventKey; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getReadAt() { return readAt; }
}
