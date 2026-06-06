package de.anonytix.feedback;

import java.util.UUID;

public interface FeedbackAnalysisSource {

    FeedbackAnalysisInput startAnalysis(UUID submissionId);

    void markReviewPending(UUID submissionId);

    void markAnalysisFailed(UUID submissionId);
}
