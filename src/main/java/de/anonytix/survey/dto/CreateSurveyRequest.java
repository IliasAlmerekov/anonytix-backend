package de.anonytix.survey.dto;

import de.anonytix.survey.domain.SurveyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateSurveyRequest(
        @NotBlank @Size(max = 240) String title,
        @Size(max = 4000) String description,
        @NotNull SurveyType type,
        @NotBlank String templateKey) {
}
