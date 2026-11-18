package com.scamshield.notifications;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface NotificationRepository extends MongoRepository<Notification, String> {
    Page<Notification> findByUserId(String userId, Pageable pageable);
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findTop5ByUserIdOrderByCreatedAtDesc(String userId);
    List<Notification> findTop10ByTypeInOrderByCreatedAtDesc(
            java.util.Collection<NotificationType> types);
    Page<Notification> findByUserIdAndRead(String userId, boolean read, Pageable pageable);
    Page<Notification> findByUserIdAndTypeIn(String userId, java.util.Collection<NotificationType> types, Pageable pageable);
    Page<Notification> findByUserIdAndReadAndTypeIn(
            String userId, boolean read, java.util.Collection<NotificationType> types, Pageable pageable);
    Optional<Notification> findByIdAndUserId(String id, String userId);
    long countByUserIdAndReadFalse(String userId);
    long deleteByIdAndUserId(String id, String userId);
    long deleteByUserId(String userId);
    long deleteByUserIdAndReadTrue(String userId);
    long countByUserId(String userId);
    boolean existsByUserIdAndEventKey(String userId, String eventKey);
}
