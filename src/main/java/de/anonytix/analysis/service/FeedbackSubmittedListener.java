package de.anonytix.analysis.service;

import de.anonytix.feedback.FeedbackAnalysisSource;
import de.anonytix.feedback.FeedbackSubmitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class FeedbackSubmittedListener {

    private static final Logger LOG =
            LoggerFactory.getLogger(FeedbackSubmittedListener.class);

    private final FeedbackAnalysisService analysisService;
    private final FeedbackAnalysisSource source;

    public FeedbackSubmittedListener(
            FeedbackAnalysisService analysisService,
            FeedbackAnalysisSource source) {
        this.analysisService = analysisService;
        this.source = source;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onFeedbackSubmitted(FeedbackSubmitted event) {
        try {
            analysisService.analyze(event.submissionId());
        } catch (RuntimeException exception) {
            LOG.error(
                    "Feedback analysis failed for submission {}",
                    event.submissionId(),
                    exception);
            source.markAnalysisFailed(event.submissionId());
        }
    }
}
