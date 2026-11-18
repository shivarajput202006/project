package com.scamshield.assistant.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.scamshield.assistant.exception.AssistantException;

@Service
public class AssistantRateLimiter {
    private static final int MAX_REQUESTS = 10;
    private static final long WINDOW_SECONDS = 60;
    private final ConcurrentHashMap<String, ArrayDeque<Instant>> requests = new ConcurrentHashMap<>();

    public void check(String userId) {
        Instant now = Instant.now();
        ArrayDeque<Instant> bucket = requests.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        synchronized (bucket) {
            Instant cutoff = now.minusSeconds(WINDOW_SECONDS);
            while (!bucket.isEmpty() && !bucket.peekFirst().isAfter(cutoff)) bucket.removeFirst();
            if (bucket.size() >= MAX_REQUESTS) throw AssistantException.rateLimited();
            bucket.addLast(now);
        }
        if (requests.size() > 5000) {
            requests.entrySet().removeIf(entry -> {
                synchronized (entry.getValue()) {
                    return entry.getValue().isEmpty()
                            || entry.getValue().peekLast().isBefore(now.minusSeconds(WINDOW_SECONDS));
                }
            });
        }
    }
}
