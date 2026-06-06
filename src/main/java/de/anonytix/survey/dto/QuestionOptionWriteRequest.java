package de.anonytix.survey.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record QuestionOptionWriteRequest(
        @NotBlank @Size(max = 300) String label,
        @NotBlank @Size(max = 100) String value,
        @Min(1) int position) {
}
