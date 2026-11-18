package com.scamshield.admin.dto;

import java.time.Instant;

public record AdminSystemHealthResponse(String database, long uptimeMillis, Instant checkedAt) {
}
