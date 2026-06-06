package de.anonytix.analysis.gateway;

import java.util.List;

public record AnalysisResult(
        String overallSentiment,
        String summary,
        boolean piiDetected,
        String riskLevel,
        String model,
        List<AnalysisFindingResult> findings) {
}
