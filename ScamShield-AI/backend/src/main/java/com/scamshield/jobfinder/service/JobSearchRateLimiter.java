package com.scamshield.jobfinder.service;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;

@Component
public class JobSearchRateLimiter {
    private static final int MAX_REQUESTS_PER_MINUTE = 30;
    private static final long WINDOW_MILLIS = 60_000;

    private final Map<String, ArrayDeque<Long>> requestsByUser = new ConcurrentHashMap<>();
    private final AtomicLong calls = new AtomicLong();

    public void check(String userId) {
        long now = Instant.now().toEpochMilli();
        ArrayDeque<Long> timestamps = requestsByUser.computeIfAbsent(userId, ignored -> new ArrayDeque<>());
        synchronized (timestamps) {
            while (!timestamps.isEmpty() && now - timestamps.peekFirst() >= WINDOW_MILLIS) {
                timestamps.removeFirst();
            }
            if (timestamps.size() >= MAX_REQUESTS_PER_MINUTE) {
                throw new RateLimitException();
            }
            timestamps.addLast(now);
        }
        if (calls.incrementAndGet() % 256 == 0) {
            requestsByUser.entrySet().removeIf(entry -> {
                synchronized (entry.getValue()) {
                    return entry.getValue().isEmpty()
                            || now - entry.getValue().peekLast() >= WINDOW_MILLIS;
                }
            });
        }
    }

    public static class RateLimitException extends RuntimeException {
        public RateLimitException() {
            super("Too many job searches. Please wait a minute and try again.");
        }

        public HttpStatusCode status() {
            return HttpStatusCode.valueOf(429);
        }
    }
}
