package de.anonytix.feedback.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "feedback_submissions")
public class FeedbackSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(name = "campaign_id", nullable = false)
    private UUID campaignId;

    @Column(name = "department_id", nullable = false)
    private UUID departmentId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SubmissionStatus status = SubmissionStatus.RECEIVED;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    @Column(name = "raw_data_delete_at")
    private Instant rawDataDeleteAt;

    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<Answer> answers = new LinkedHashSet<>();

    protected FeedbackSubmission() {
    }

    public FeedbackSubmission(UUID companyId, UUID campaignId, UUID departmentId) {
        this.companyId = companyId;
        this.campaignId = campaignId;
        this.departmentId = departmentId;
        this.submittedAt = Instant.now();
        this.rawDataDeleteAt = submittedAt.plusSeconds(60L * 60 * 24 * 30);
    }

    public void addAnswer(Answer answer) {
        answers.add(answer);
    }

    public UUID getId() {
        return id;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public UUID getCampaignId() {
        return campaignId;
    }

    public UUID getDepartmentId() {
        return departmentId;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public Set<Answer> getAnswers() {
        return Set.copyOf(answers);
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void markAnalyzing() {
        status = SubmissionStatus.ANALYZING;
    }

    public void markReviewPending() {
        status = SubmissionStatus.REVIEW_PENDING;
    }

    public void markAnalysisFailed() {
        status = SubmissionStatus.ANALYSIS_FAILED;
    }

    public void approve() {
        requireReviewPending();
        status = SubmissionStatus.APPROVED;
    }

    public void reject() {
        requireReviewPending();
        status = SubmissionStatus.REJECTED;
    }

    private void requireReviewPending() {
        if (status != SubmissionStatus.REVIEW_PENDING) {
            throw new IllegalStateException(
                    "Nur Feedback in der Moderationsprüfung kann entschieden werden.");
        }
    }
}
