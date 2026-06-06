package de.anonytix.moderation.dto;

import java.time.Instant;
import java.util.UUID;

public record ModerationSummary(
        UUID submissionId,
        String status,
        Instant submittedAt,
        String riskLevel) {
}
