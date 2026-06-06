package de.anonytix.survey.dto;

import de.anonytix.survey.domain.QuestionType;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public record QuestionResponse(
        UUID id,
        String text,
        String helpText,
        QuestionType type,
        String category,
        boolean required,
        int position,
        Integer minimumValue,
        Integer maximumValue,
        Integer maximumLength,
        boolean analyzeWithAi,
        Set<UUID> departmentIds,
        List<QuestionOptionResponse> options) {
}
