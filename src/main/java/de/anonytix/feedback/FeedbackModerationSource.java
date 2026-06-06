package de.anonytix.feedback;

import java.util.List;
import java.util.UUID;

public interface FeedbackModerationSource {

    List<ModerationSubmissionData> findByStatus(String status);

    ModerationSubmissionData get(UUID submissionId);

    ModerationSubmissionData approve(UUID submissionId);

    ModerationSubmissionData reject(UUID submissionId);
}
