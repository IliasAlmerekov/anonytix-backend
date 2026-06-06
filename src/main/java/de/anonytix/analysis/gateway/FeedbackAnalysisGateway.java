package de.anonytix.analysis.gateway;

import de.anonytix.feedback.FeedbackAnalysisInput;

public interface FeedbackAnalysisGateway {

    AnalysisResult analyze(FeedbackAnalysisInput input);
}
