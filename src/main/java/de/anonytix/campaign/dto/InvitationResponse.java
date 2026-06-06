package de.anonytix.campaign.dto;

import java.time.Instant;
import java.util.UUID;

public record InvitationResponse(
        UUID campaignId,
        String token,
        String url,
        Instant expiresAt) {
}
