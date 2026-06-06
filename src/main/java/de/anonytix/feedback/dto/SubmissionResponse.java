package de.anonytix.feedback.dto;

import de.anonytix.feedback.domain.SubmissionStatus;
import java.time.Instant;
import java.util.UUID;

public record SubmissionResponse(
        UUID submissionId,
        SubmissionStatus status,
        Instant submittedAt,
        String message) {
}
