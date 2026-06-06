package de.anonytix.campaign.service;

import de.anonytix.campaign.domain.Campaign;
import de.anonytix.campaign.domain.CampaignStatus;
import de.anonytix.campaign.dto.CampaignResponse;
import de.anonytix.campaign.dto.CreateCampaignRequest;
import de.anonytix.campaign.mapper.CampaignMapper;
import de.anonytix.campaign.repository.CampaignRepository;
import de.anonytix.company.CompanyDirectory;
import de.anonytix.shared.error.ConflictException;
import de.anonytix.shared.error.ResourceNotFoundException;
import de.anonytix.survey.SurveyDescriptor;
import de.anonytix.survey.SurveyDirectory;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CampaignService {

    private final CompanyDirectory companyDirectory;
    private final SurveyDirectory surveyDirectory;
    private final CampaignRepository campaignRepository;
    private final CampaignMapper campaignMapper;

    public CampaignService(
            CompanyDirectory companyDirectory,
            SurveyDirectory surveyDirectory,
            CampaignRepository campaignRepository,
            CampaignMapper campaignMapper) {
        this.companyDirectory = companyDirectory;
        this.surveyDirectory = surveyDirectory;
        this.campaignRepository = campaignRepository;
        this.campaignMapper = campaignMapper;
    }

    public List<CampaignResponse> list(UUID companyId) {
        requireCompany(companyId);
        return campaignRepository.findAllByCompanyIdOrderByStartsAtDesc(companyId).stream()
                .map(campaignMapper::toResponse)
                .toList();
    }

    @Transactional
    public CampaignResponse create(UUID companyId, CreateCampaignRequest request) {
        requireCompany(companyId);
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new IllegalArgumentException("Das Kampagnenende muss nach dem Start liegen.");
        }
        SurveyDescriptor survey = surveyDirectory.find(companyId, request.surveyId())
                .orElseThrow(() -> new ResourceNotFoundException("Umfrage wurde nicht gefunden."));
        if (!survey.published()) {
            throw new ConflictException(
                    "SURVEY_NOT_PUBLISHED",
                    "Eine Kampagne benötigt eine veröffentlichte Umfrage.");
        }
        Campaign campaign = campaignRepository.save(new Campaign(
                companyId,
                request.surveyId(),
                request.name().trim(),
                request.startsAt(),
                request.endsAt()));
        return campaignMapper.toResponse(campaign);
    }

    @Transactional
    public CampaignResponse activate(UUID companyId, UUID campaignId) {
        Campaign campaign = requireCampaign(companyId, campaignId);
        if (campaign.getStatus() != CampaignStatus.SCHEDULED) {
            throw new ConflictException(
                    "CAMPAIGN_NOT_SCHEDULED",
                    "Nur geplante Kampagnen können aktiviert werden.");
        }
        campaign.activate();
        return campaignMapper.toResponse(campaign);
    }

    public Campaign requireCampaign(UUID companyId, UUID campaignId) {
        return campaignRepository.findByIdAndCompanyId(campaignId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Kampagne wurde nicht gefunden."));
    }

    private void requireCompany(UUID companyId) {
        if (!companyDirectory.companyExists(companyId)) {
            throw new ResourceNotFoundException("Firma wurde nicht gefunden.");
        }
    }
}
