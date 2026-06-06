package de.anonytix.feedback.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record SubmitFeedbackRequest(
        @NotNull UUID departmentId,
        @NotEmpty List<@Valid AnswerRequest> answers) {
}
