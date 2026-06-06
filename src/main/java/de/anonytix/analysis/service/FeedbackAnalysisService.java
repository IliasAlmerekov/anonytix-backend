package de.anonytix.analysis.service;

import de.anonytix.analysis.domain.AiAnalysis;
import de.anonytix.analysis.gateway.AnalysisResult;
import de.anonytix.analysis.gateway.FeedbackAnalysisGateway;
import de.anonytix.analysis.repository.AiAnalysisRepository;
import de.anonytix.feedback.FeedbackAnalysisInput;
import de.anonytix.feedback.FeedbackAnalysisSource;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackAnalysisService {

    private final FeedbackAnalysisSource source;
    private final FeedbackAnalysisGateway gateway;
    private final AiAnalysisRepository repository;

    public FeedbackAnalysisService(
            FeedbackAnalysisSource source,
            FeedbackAnalysisGateway gateway,
            AiAnalysisRepository repository) {
        this.source = source;
        this.gateway = gateway;
        this.repository = repository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void analyze(UUID submissionId) {
        FeedbackAnalysisInput input = source.startAnalysis(submissionId);
        AnalysisResult result = gateway.analyze(input);
        repository.save(new AiAnalysis(submissionId, result));
        source.markReviewPending(submissionId);
    }
}
