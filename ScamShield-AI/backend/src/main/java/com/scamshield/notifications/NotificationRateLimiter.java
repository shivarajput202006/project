package com.scamshield.notifications;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Service;


@Service
public class NotificationRateLimiter {
    private final ConcurrentHashMap<String, ArrayDeque<Instant>> requests = new ConcurrentHashMap<>();
    private final AtomicLong calls = new AtomicLong();

    public void check(String userId) {
        Instant now = Instant.now();
        requests.compute(userId, (ignored, current) -> {
            ArrayDeque<Instant> bucket = current == null ? new ArrayDeque<>() : current;
            while (!bucket.isEmpty() && bucket.peekFirst().isBefore(now.minusSeconds(60))) {
                bucket.removeFirst();
            }
            if (bucket.size() >= 60) {
                throw NotificationException.rateLimited();
            }
            bucket.addLast(now);
            return bucket;
        });
        if (calls.incrementAndGet() % 256 == 0) {
            Instant cutoff = now.minusSeconds(60);
            requests.keySet().forEach(key -> requests.computeIfPresent(key, (ignored, bucket) -> {
                if (bucket.isEmpty() || !bucket.peekLast().isAfter(cutoff)) {
                    return null;
                }
                return bucket;
            }));
        }
    }
}
