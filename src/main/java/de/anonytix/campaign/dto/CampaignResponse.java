package de.anonytix.campaign.dto;

import de.anonytix.campaign.domain.CampaignStatus;
import java.time.Instant;
import java.util.UUID;

public record CampaignResponse(
        UUID id,
        UUID surveyId,
        String name,
        Instant startsAt,
        Instant endsAt,
        CampaignStatus status) {
}
