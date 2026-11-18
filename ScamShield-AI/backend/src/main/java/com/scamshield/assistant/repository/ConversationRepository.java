package com.scamshield.assistant.repository;

import java.util.Optional;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.scamshield.assistant.model.Conversation;

public interface ConversationRepository extends MongoRepository<Conversation, String> {
    Page<Conversation> findByUserId(String userId, Pageable pageable);
    List<Conversation> findAllByUserIdOrderByUpdatedAtDesc(String userId);
    Optional<Conversation> findByIdAndUserId(String id, String userId);
    long deleteByIdAndUserId(String id, String userId);
    long deleteByUserId(String userId);
}
