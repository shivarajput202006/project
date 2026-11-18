package com.scamshield.notifications;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.scamshield.notifications.dto.NotificationCountResponse;
import com.scamshield.notifications.dto.NotificationResponse;

@Service
public class NotificationReadService {
    private static final Pattern OBJECT_ID = Pattern.compile("[a-fA-F0-9]{24}");
    private final NotificationRepository repository;
    private final MongoTemplate mongoTemplate;

    public NotificationReadService(NotificationRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    public Page<NotificationResponse> list(String userId, String filter, int page, int size) {
        String normalized = filter == null || filter.isBlank() ? "ALL" : filter.trim().toUpperCase(Locale.ROOT);
        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Notification> result = switch (normalized) {
            case "ALL" -> repository.findByUserId(userId, pageable);
            case "UNREAD" -> repository.findByUserIdAndRead(userId, false, pageable);
            case "SECURITY" -> repository.findByUserIdAndTypeIn(
                    userId, List.of(NotificationType.SECURITY), pageable);
            case "JOBS" -> repository.findByUserIdAndTypeIn(
                    userId, List.of(NotificationType.JOB, NotificationType.JOB_MATCH), pageable);
            case "SYSTEM" -> repository.findByUserIdAndTypeIn(
                    userId, List.of(NotificationType.SYSTEM, NotificationType.ADMIN), pageable);
            case "SCAM_ANALYSIS" -> repository.findByUserIdAndTypeIn(
                    userId, List.of(NotificationType.SCAM_ANALYSIS), pageable);
            case "URL_SCAN" -> repository.findByUserIdAndTypeIn(userId, List.of(NotificationType.URL_SCAN), pageable);
            case "RESUME" -> repository.findByUserIdAndTypeIn(userId, List.of(NotificationType.RESUME), pageable);
            case "JOB_MATCH" -> repository.findByUserIdAndTypeIn(userId, List.of(NotificationType.JOB_MATCH), pageable);
            case "REPORT" -> repository.findByUserIdAndTypeIn(userId, List.of(NotificationType.REPORT), pageable);
            default -> throw NotificationException.invalid("Notification filter is not supported.");
        };
        return result.map(NotificationResponse::from);
    }

    public NotificationCountResponse unreadCount(String userId) {
        return new NotificationCountResponse(repository.countByUserIdAndReadFalse(userId));
    }

    public NotificationResponse get(String userId, String id) {
        return NotificationResponse.from(findOwned(userId, id));
    }

    public NotificationResponse markRead(String userId, String id) {
        Notification notification = findOwned(userId, id);
        if (!notification.isRead()) {
            notification.markRead(Instant.now());
            notification = repository.save(notification);
        }
        return NotificationResponse.from(notification);
    }

    public NotificationResponse markUnread(String userId, String id) {
        Notification notification = findOwned(userId, id);
        if (notification.isRead()) {
            notification.markUnread();
            notification = repository.save(notification);
        }
        return NotificationResponse.from(notification);
    }

    public void markAllRead(String userId) {
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("userId").is(userId).and("read").is(false)),
                new Update().set("read", true).set("readAt", Instant.now()),
                Notification.class);
    }

    public void delete(String userId, String id) {
        validateId(id);
        if (repository.deleteByIdAndUserId(id, userId) == 0) throw NotificationException.notFound();
    }

    public void deleteAll(String userId) {
        repository.deleteByUserId(userId);
    }

    private Notification findOwned(String userId, String id) {
        validateId(id);
        return repository.findByIdAndUserId(id, userId).orElseThrow(NotificationException::notFound);
    }

    private void validateId(String id) {
        if (id == null || !OBJECT_ID.matcher(id).matches()) {
            throw NotificationException.invalid("Notification ID is invalid.");
        }
    }
}
