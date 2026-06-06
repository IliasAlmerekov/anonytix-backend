package de.anonytix.moderation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "moderation_reviews")
public class ModerationReview {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "submission_id", nullable = false)
    private UUID submissionId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ModerationDecision decision;

    @Column(columnDefinition = "TEXT")
    private String reason;

    @Column(name = "reviewed_at", nullable = false)
    private Instant reviewedAt;

    protected ModerationReview() {
    }

    public ModerationReview(
            UUID submissionId,
            ModerationDecision decision,
            String reason) {
        this.submissionId = submissionId;
        this.decision = decision;
        this.reason = reason;
        this.reviewedAt = Instant.now();
    }
}
