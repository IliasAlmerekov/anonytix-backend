package de.anonytix.campaign;

import java.time.Instant;
import java.util.UUID;

public record InvitationDescriptor(
        String tokenHash,
        UUID campaignId,
        UUID companyId,
        UUID surveyId,
        String campaignName,
        String campaignStatus,
        Instant campaignEndsAt,
        String invitationStatus,
        Instant expiresAt) {
}
