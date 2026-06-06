package de.anonytix.feedback;

import java.util.List;
import java.util.UUID;

public record FeedbackAnalysisInput(
        UUID submissionId,
        UUID companyId,
        UUID campaignId,
        UUID departmentId,
        List<FeedbackAnswerData> answers) {
}
