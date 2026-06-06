package de.anonytix.moderation.dto;

import de.anonytix.analysis.AnalysisFindingDescriptor;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ModerationDetail(
        UUID submissionId,
        String status,
        Instant submittedAt,
        String riskLevel,
        String summary,
        boolean piiDetected,
        String model,
        List<AnalysisFindingDescriptor> findings) {
}
