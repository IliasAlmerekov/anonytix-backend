package de.anonytix.campaign.domain;

import de.anonytix.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "campaigns")
public class Campaign extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "survey_id", nullable = false)
    private UUID surveyId;

    @Column(nullable = false, length = 240)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CampaignStatus status = CampaignStatus.SCHEDULED;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    protected Campaign() {
    }

    public Campaign(
            UUID companyId,
            UUID surveyId,
            String name,
            Instant startsAt,
            Instant endsAt) {
        this.companyId = companyId;
        this.surveyId = surveyId;
        this.name = name;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
    }

    public void activate() {
        status = CampaignStatus.ACTIVE;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getSurveyId() {
        return surveyId;
    }

    public String getName() {
        return name;
    }

    public CampaignStatus getStatus() {
        return status;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }
}
