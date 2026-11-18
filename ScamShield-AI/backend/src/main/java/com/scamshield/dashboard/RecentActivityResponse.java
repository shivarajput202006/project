package com.scamshield.dashboard;

import java.time.Instant;

public record RecentActivityResponse(
        String id,
        String type,
        String title,
        String description,
        Instant createdAt,
        String status) {
}
