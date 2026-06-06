package de.anonytix.campaign.web;

import de.anonytix.campaign.dto.CampaignResponse;
import de.anonytix.campaign.dto.CreateCampaignRequest;
import de.anonytix.campaign.dto.GenerateInvitationRequest;
import de.anonytix.campaign.dto.InvitationResponse;
import de.anonytix.campaign.service.CampaignService;
import de.anonytix.campaign.service.InvitationService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final InvitationService invitationService;

    public CampaignController(
            CampaignService campaignService,
            InvitationService invitationService) {
        this.campaignService = campaignService;
        this.invitationService = invitationService;
    }

    @GetMapping
    List<CampaignResponse> list(@PathVariable UUID companyId) {
        return campaignService.list(companyId);
    }

    @PostMapping
    ResponseEntity<CampaignResponse> create(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateCampaignRequest request) {
        CampaignResponse created = campaignService.create(companyId, request);
        return ResponseEntity.created(URI.create(
                        "/api/v1/companies/%s/campaigns/%s".formatted(companyId, created.id())))
                .body(created);
    }

    @PostMapping("/{campaignId}/activate")
    CampaignResponse activate(
            @PathVariable UUID companyId,
            @PathVariable UUID campaignId) {
        return campaignService.activate(companyId, campaignId);
    }

    @PostMapping("/{campaignId}/invitations")
    ResponseEntity<InvitationResponse> generateInvitation(
            @PathVariable UUID companyId,
            @PathVariable UUID campaignId,
            @Valid @RequestBody GenerateInvitationRequest request) {
        return ResponseEntity.status(201)
                .body(invitationService.generate(companyId, campaignId, request));
    }
}
