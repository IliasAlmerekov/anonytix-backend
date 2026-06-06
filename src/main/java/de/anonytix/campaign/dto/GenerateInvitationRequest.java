package de.anonytix.campaign.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record GenerateInvitationRequest(@NotNull @Future Instant expiresAt) {
}
