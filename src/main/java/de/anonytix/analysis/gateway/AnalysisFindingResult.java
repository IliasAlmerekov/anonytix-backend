package de.anonytix.analysis.gateway;

public record AnalysisFindingResult(
        String category,
        String label,
        String sentiment,
        String priority,
        double score,
        String anonymizedText) {
}
