package de.anonytix.campaign.service;

import de.anonytix.campaign.domain.Campaign;
import de.anonytix.campaign.domain.CampaignStatus;
import de.anonytix.campaign.domain.Invitation;
import de.anonytix.campaign.dto.GenerateInvitationRequest;
import de.anonytix.campaign.dto.InvitationResponse;
import de.anonytix.campaign.repository.InvitationRepository;
import de.anonytix.shared.error.ConflictException;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InvitationService {

    private final CampaignService campaignService;
    private final InvitationRepository invitationRepository;
    private final TokenService tokenService;
    private final String frontendUrl;

    public InvitationService(
            CampaignService campaignService,
            InvitationRepository invitationRepository,
            TokenService tokenService,
            @Value("${anonytix.frontend-url}") String frontendUrl) {
        this.campaignService = campaignService;
        this.invitationRepository = invitationRepository;
        this.tokenService = tokenService;
        this.frontendUrl = frontendUrl;
    }

    @Transactional
    public InvitationResponse generate(
            UUID companyId,
            UUID campaignId,
            GenerateInvitationRequest request) {
        Campaign campaign = campaignService.requireCampaign(companyId, campaignId);
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new ConflictException(
                    "CAMPAIGN_NOT_ACTIVE",
                    "Einladungen können nur für aktive Kampagnen erzeugt werden.");
        }
        if (request.expiresAt().isAfter(campaign.getEndsAt())) {
            throw new IllegalArgumentException(
                    "Der Einladungslink darf nicht nach Kampagnenende ablaufen.");
        }
        String token = tokenService.generate();
        invitationRepository.save(new Invitation(
                campaignId,
                tokenService.hash(token),
                request.expiresAt()));
        return new InvitationResponse(
                campaignId,
                token,
                frontendUrl + "/feedback/" + token,
                request.expiresAt());
    }
}
