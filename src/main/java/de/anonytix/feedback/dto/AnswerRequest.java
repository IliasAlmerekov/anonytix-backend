package de.anonytix.feedback.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AnswerRequest(
        @NotNull UUID questionId,
        BigDecimal numericValue,
        String textValue,
        Boolean booleanValue,
        List<UUID> selectedOptionIds) {

    public AnswerRequest {
        selectedOptionIds = selectedOptionIds == null ? List.of() : List.copyOf(selectedOptionIds);
    }
}
