package de.anonytix.survey.dto;

import jakarta.validation.constraints.Size;

public record UpdateSurveyRequest(
        @Size(min = 1, max = 240) String title,
        @Size(max = 4000) String description) {
}
