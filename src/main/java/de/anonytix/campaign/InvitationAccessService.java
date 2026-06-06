package de.anonytix.campaign;

import de.anonytix.campaign.domain.Campaign;
import de.anonytix.campaign.domain.CampaignStatus;
import de.anonytix.campaign.domain.Invitation;
import de.anonytix.campaign.domain.InvitationStatus;
import de.anonytix.campaign.repository.CampaignRepository;
import de.anonytix.campaign.repository.InvitationRepository;
import de.anonytix.campaign.service.TokenService;
import de.anonytix.shared.error.ConflictException;
import de.anonytix.shared.error.GoneException;
import de.anonytix.shared.error.ResourceNotFoundException;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class InvitationAccessService implements InvitationAccess {

    private final InvitationRepository invitationRepository;
    private final CampaignRepository campaignRepository;
    private final TokenService tokenService;

    InvitationAccessService(
            InvitationRepository invitationRepository,
            CampaignRepository campaignRepository,
            TokenService tokenService) {
        this.invitationRepository = invitationRepository;
        this.campaignRepository = campaignRepository;
        this.tokenService = tokenService;
    }

    @Override
    @Transactional(readOnly = true)
    public InvitationDescriptor resolve(String clearToken) {
        Invitation invitation = invitationRepository.findByTokenHash(tokenService.hash(clearToken))
                .orElseThrow(() -> new ResourceNotFoundException("Einladungslink wurde nicht gefunden."));
        return validateAndDescribe(invitation);
    }

    @Override
    @Transactional
    public InvitationDescriptor lock(String clearToken) {
        Invitation invitation = invitationRepository
                .findByTokenHashForUpdate(tokenService.hash(clearToken))
                .orElseThrow(() -> new ResourceNotFoundException("Einladungslink wurde nicht gefunden."));
        return validateAndDescribe(invitation);
    }

    @Override
    @Transactional
    public void markUsed(String tokenHash) {
        Invitation invitation = invitationRepository.findByTokenHashForUpdate(tokenHash)
                .orElseThrow(() -> new ResourceNotFoundException("Einladungslink wurde nicht gefunden."));
        invitation.markUsed();
    }

    private InvitationDescriptor validateAndDescribe(Invitation invitation) {
        if (invitation.getStatus() == InvitationStatus.USED) {
            throw new ConflictException(
                    "INVITATION_ALREADY_USED",
                    "Dieser Einladungslink wurde bereits verwendet.");
        }
        if (invitation.getStatus() != InvitationStatus.ACTIVE) {
            throw new ConflictException(
                    "INVITATION_NOT_ACTIVE",
                    "Dieser Einladungslink ist nicht aktiv.");
        }
        if (!invitation.getExpiresAt().isAfter(Instant.now())) {
            throw new GoneException(
                    "INVITATION_EXPIRED",
                    "Dieser Einladungslink ist abgelaufen.");
        }
        Campaign campaign = campaignRepository.findById(invitation.getCampaignId())
                .orElseThrow(() -> new ResourceNotFoundException("Kampagne wurde nicht gefunden."));
        if (campaign.getStatus() != CampaignStatus.ACTIVE) {
            throw new ConflictException(
                    "CAMPAIGN_NOT_ACTIVE",
                    "Die Kampagne ist nicht aktiv.");
        }
        return new InvitationDescriptor(
                invitation.getTokenHash(),
                campaign.getId(),
                campaign.getCompanyId(),
                campaign.getSurveyId(),
                campaign.getName(),
                campaign.getStatus().name(),
                campaign.getEndsAt(),
                invitation.getStatus().name(),
                invitation.getExpiresAt());
    }
}
