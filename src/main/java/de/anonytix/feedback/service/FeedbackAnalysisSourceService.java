package de.anonytix.feedback.service;

import de.anonytix.feedback.FeedbackAnalysisInput;
import de.anonytix.feedback.FeedbackAnalysisSource;
import de.anonytix.feedback.FeedbackAnswerData;
import de.anonytix.feedback.domain.FeedbackSubmission;
import de.anonytix.feedback.domain.SubmissionStatus;
import de.anonytix.feedback.repository.FeedbackSubmissionRepository;
import de.anonytix.shared.error.ConflictException;
import de.anonytix.shared.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackAnalysisSourceService implements FeedbackAnalysisSource {

    private final FeedbackSubmissionRepository repository;

    public FeedbackAnalysisSourceService(FeedbackSubmissionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional
    public FeedbackAnalysisInput startAnalysis(UUID submissionId) {
        FeedbackSubmission submission = findWithAnswers(submissionId);
        if (submission.getStatus() != SubmissionStatus.RECEIVED
                && submission.getStatus() != SubmissionStatus.ANALYSIS_FAILED) {
            throw new ConflictException(
                    "SUBMISSION_NOT_ANALYZABLE",
                    "Dieses Feedback kann in seinem aktuellen Status nicht analysiert werden.");
        }
        submission.markAnalyzing();
        return new FeedbackAnalysisInput(
                submission.getId(),
                submission.getCompanyId(),
                submission.getCampaignId(),
                submission.getDepartmentId(),
                submission.getAnswers().stream()
                        .map(answer -> new FeedbackAnswerData(
                                answer.getQuestionId(),
                                answer.getNumericValue(),
                                answer.getTextValue(),
                                answer.getBooleanValue(),
                                answer.getSelectedOptionIds()))
                        .toList());
    }

    @Override
    @Transactional
    public void markReviewPending(UUID submissionId) {
        find(submissionId).markReviewPending();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markAnalysisFailed(UUID submissionId) {
        find(submissionId).markAnalysisFailed();
    }

    private FeedbackSubmission findWithAnswers(UUID submissionId) {
        return repository.findForAnalysis(submissionId)
                .orElseThrow(() -> notFound(submissionId));
    }

    private FeedbackSubmission find(UUID submissionId) {
        return repository.findById(submissionId)
                .orElseThrow(() -> notFound(submissionId));
    }

    private ResourceNotFoundException notFound(UUID submissionId) {
        return new ResourceNotFoundException(
                "Feedback-Abgabe " + submissionId + " wurde nicht gefunden.");
    }
}
