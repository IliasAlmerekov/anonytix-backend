package de.anonytix.feedback;

import java.math.BigDecimal;
import java.util.Set;
import java.util.UUID;

public record FeedbackAnswerData(
        UUID questionId,
        BigDecimal numericValue,
        String textValue,
        Boolean booleanValue,
        Set<UUID> selectedOptionIds) {
}
