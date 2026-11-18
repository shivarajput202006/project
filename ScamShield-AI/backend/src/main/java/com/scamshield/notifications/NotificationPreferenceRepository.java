package com.scamshield.notifications;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface NotificationPreferenceRepository extends MongoRepository<NotificationPreference, String> {
    Optional<NotificationPreference> findByUserId(String userId);
    long deleteByUserId(String userId);
}
