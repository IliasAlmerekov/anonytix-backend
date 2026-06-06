package de.anonytix.feedback;

import java.time.Instant;
import java.util.UUID;

public record ModerationSubmissionData(
        UUID submissionId,
        String status,
        Instant submittedAt) {
}
