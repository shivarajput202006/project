package com.scamshield.admin.service;

import java.time.Instant;

import org.bson.Document;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import com.scamshield.admin.dto.AdminSystemHealthResponse;

@Service
public class AdminSystemHealthService {
    private final MongoTemplate mongoTemplate;
    private final long startedAtMillis = System.currentTimeMillis();

    public AdminSystemHealthService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public AdminSystemHealthResponse getHealth() {
        String database;
        try {
            mongoTemplate.executeCommand(new Document("ping", 1));
            database = "UP";
        } catch (DataAccessException exception) {
            database = "DOWN";
        }
        return new AdminSystemHealthResponse(
                database,
                Math.max(0, System.currentTimeMillis() - startedAtMillis),
                Instant.now());
    }
}
