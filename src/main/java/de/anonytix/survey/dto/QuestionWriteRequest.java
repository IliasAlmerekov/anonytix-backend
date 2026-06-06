package de.anonytix.survey.dto;

import de.anonytix.survey.domain.QuestionType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record QuestionWriteRequest(
        @NotBlank @Size(max = 1000) String text,
        @Size(max = 1000) String helpText,
        @NotNull QuestionType type,
        @NotBlank @Size(max = 80) String category,
        boolean required,
        Integer minimumValue,
        Integer maximumValue,
        Integer maximumLength,
        boolean analyzeWithAi,
        List<UUID> departmentIds,
        List<@Valid QuestionOptionWriteRequest> options) {

    public QuestionWriteRequest {
        departmentIds = departmentIds == null ? List.of() : List.copyOf(departmentIds);
        options = options == null ? List.of() : List.copyOf(options);
    }
}
