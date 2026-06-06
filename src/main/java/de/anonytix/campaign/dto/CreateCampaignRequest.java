package de.anonytix.campaign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import java.util.UUID;

public record CreateCampaignRequest(
        @NotNull UUID surveyId,
        @NotBlank @Size(max = 240) String name,
        @NotNull Instant startsAt,
        @NotNull Instant endsAt) {
}
