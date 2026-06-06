package de.anonytix.moderation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RejectModerationRequest(
        @NotBlank @Size(max = 1000) String reason) {
}
