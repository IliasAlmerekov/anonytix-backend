package de.anonytix.analysis;

import java.math.BigDecimal;

public record AnalysisFindingDescriptor(
        String category,
        String label,
        String sentiment,
        String priority,
        BigDecimal score,
        String anonymizedText) {
}
