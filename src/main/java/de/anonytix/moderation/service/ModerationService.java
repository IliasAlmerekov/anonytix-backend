package de.anonytix.moderation.service;

import de.anonytix.analysis.AnalysisDescriptor;
import de.anonytix.analysis.AnalysisDirectory;
import de.anonytix.feedback.FeedbackModerationSource;
import de.anonytix.feedback.ModerationSubmissionData;
import de.anonytix.moderation.domain.ModerationDecision;
import de.anonytix.moderation.domain.ModerationReview;
import de.anonytix.moderation.dto.ModerationDetail;
import de.anonytix.moderation.dto.ModerationSummary;
import de.anonytix.moderation.repository.ModerationReviewRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ModerationService {

    private final FeedbackModerationSource feedbackSource;
    private final AnalysisDirectory analysisDirectory;
    private final ModerationReviewRepository reviewRepository;

    public ModerationService(
            FeedbackModerationSource feedbackSource,
            AnalysisDirectory analysisDirectory,
            ModerationReviewRepository reviewRepository) {
        this.feedbackSource = feedbackSource;
        this.analysisDirectory = analysisDirectory;
        this.reviewRepository = reviewRepository;
    }

    @Transactional(readOnly = true)
    public List<ModerationSummary> list(String status) {
        return feedbackSource.findByStatus(status).stream()
                .map(this::toSummary)
                .toList();
    }

    @Transactional(readOnly = true)
    public ModerationDetail get(UUID submissionId) {
        ModerationSubmissionData submission = feedbackSource.get(submissionId);
        AnalysisDescriptor analysis = analysisDirectory.getBySubmissionId(submissionId);
        return new ModerationDetail(
                submission.submissionId(),
                submission.status(),
                submission.submittedAt(),
                analysis.riskLevel(),
                analysis.summary(),
                analysis.piiDetected(),
                analysis.model(),
                analysis.findings());
    }

    @Transactional
    public ModerationSummary approve(UUID submissionId) {
        ModerationSubmissionData submission = feedbackSource.approve(submissionId);
        reviewRepository.save(new ModerationReview(
                submissionId, ModerationDecision.APPROVED, null));
        return toSummary(submission);
    }

    @Transactional
    public ModerationSummary reject(UUID submissionId, String reason) {
        ModerationSubmissionData submission = feedbackSource.reject(submissionId);
        reviewRepository.save(new ModerationReview(
                submissionId, ModerationDecision.REJECTED, reason));
        return toSummary(submission);
    }

    private ModerationSummary toSummary(ModerationSubmissionData submission) {
        AnalysisDescriptor analysis =
                analysisDirectory.getBySubmissionId(submission.submissionId());
        return new ModerationSummary(
                submission.submissionId(),
                submission.status(),
                submission.submittedAt(),
                analysis.riskLevel());
    }
}
