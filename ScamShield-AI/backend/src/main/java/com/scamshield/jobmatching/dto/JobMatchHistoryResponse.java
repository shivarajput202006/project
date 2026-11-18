package com.scamshield.jobmatching.dto;

import java.time.Instant;

import com.scamshield.jobmatching.enums.MatchStatus;

public record JobMatchHistoryResponse(
        String id,
        String resumeId,
        String sourceName,
        String sourceJobId,
        Integer score,
        String scoringModelVersion,
        MatchStatus status,
        Instant analyzedAt) {
}
