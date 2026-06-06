package de.anonytix.analysis;

import java.util.List;
import java.util.UUID;

public record AnalysisDescriptor(
        UUID submissionId,
        String overallSentiment,
        String summary,
        boolean piiDetected,
        String riskLevel,
        String model,
        List<AnalysisFindingDescriptor> findings) {
}
