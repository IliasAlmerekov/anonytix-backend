package de.anonytix.feedback.service;

import de.anonytix.feedback.FeedbackModerationSource;
import de.anonytix.feedback.ModerationSubmissionData;
import de.anonytix.feedback.domain.FeedbackSubmission;
import de.anonytix.feedback.domain.SubmissionStatus;
import de.anonytix.feedback.repository.FeedbackSubmissionRepository;
import de.anonytix.shared.error.ConflictException;
import de.anonytix.shared.error.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackModerationSourceService implements FeedbackModerationSource {

    private final FeedbackSubmissionRepository repository;

    public FeedbackModerationSourceService(FeedbackSubmissionRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ModerationSubmissionData> findByStatus(String status) {
        SubmissionStatus submissionStatus;
        try {
            submissionStatus = SubmissionStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unbekannter Feedback-Status: " + status);
        }
        return repository.findAllByStatusOrderBySubmittedAtAsc(submissionStatus).stream()
                .map(this::toData)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ModerationSubmissionData get(UUID submissionId) {
        return toData(find(submissionId));
    }

    @Override
    @Transactional
    public ModerationSubmissionData approve(UUID submissionId) {
        FeedbackSubmission submission = find(submissionId);
        transition(submission, true);
        return toData(submission);
    }

    @Override
    @Transactional
    public ModerationSubmissionData reject(UUID submissionId) {
        FeedbackSubmission submission = find(submissionId);
        transition(submission, false);
        return toData(submission);
    }

    private void transition(FeedbackSubmission submission, boolean approve) {
        try {
            if (approve) {
                submission.approve();
            } else {
                submission.reject();
            }
        } catch (IllegalStateException exception) {
            throw new ConflictException(
                    "SUBMISSION_ALREADY_MODERATED",
                    exception.getMessage());
        }
    }

    private FeedbackSubmission find(UUID submissionId) {
        return repository.findById(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Feedback-Abgabe " + submissionId + " wurde nicht gefunden."));
    }

    private ModerationSubmissionData toData(FeedbackSubmission submission) {
        return new ModerationSubmissionData(
                submission.getId(),
                submission.getStatus().name(),
                submission.getSubmittedAt());
    }
}
