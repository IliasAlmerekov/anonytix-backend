package de.anonytix.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.anonytix.analysis.domain.AiAnalysis;
import de.anonytix.analysis.gateway.AnalysisFindingResult;
import de.anonytix.analysis.gateway.AnalysisResult;
import de.anonytix.analysis.gateway.FeedbackAnalysisGateway;
import de.anonytix.analysis.repository.AiAnalysisRepository;
import de.anonytix.analysis.service.FeedbackAnalysisService;
import de.anonytix.feedback.FeedbackAnalysisInput;
import de.anonytix.feedback.FeedbackAnalysisSource;
import de.anonytix.feedback.FeedbackAnswerData;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class FeedbackAnalysisServiceTest {

    private static final UUID SUBMISSION_ID = UUID.randomUUID();

    @Test
    void storesStructuredAnalysisAndMovesSubmissionToModeration() {
        FeedbackAnalysisSource source = mock(FeedbackAnalysisSource.class);
        FeedbackAnalysisGateway gateway = mock(FeedbackAnalysisGateway.class);
        AiAnalysisRepository repository = mock(AiAnalysisRepository.class);
        FeedbackAnalysisInput input = new FeedbackAnalysisInput(
                SUBMISSION_ID,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(new FeedbackAnswerData(
                        UUID.randomUUID(),
                        null,
                        "Ich bin wegen der hohen Arbeitslast überlastet.",
                        null,
                        Set.of())));
        AnalysisResult result = new AnalysisResult(
                "NEGATIVE",
                "Die Arbeitsbelastung wird kritisch bewertet.",
                false,
                "HIGH",
                "demo-analysis-v1",
                List.of(new AnalysisFindingResult(
                        "WORKLOAD",
                        "Hohe Arbeitsbelastung",
                        "NEGATIVE",
                        "HIGH",
                        0.92,
                        "Die Arbeitsbelastung wird als zu hoch beschrieben.")));
        when(source.startAnalysis(SUBMISSION_ID)).thenReturn(input);
        when(gateway.analyze(input)).thenReturn(result);

        new FeedbackAnalysisService(source, gateway, repository).analyze(SUBMISSION_ID);

        ArgumentCaptor<AiAnalysis> analysisCaptor =
                ArgumentCaptor.forClass(AiAnalysis.class);
        verify(repository).save(analysisCaptor.capture());
        verify(source).markReviewPending(SUBMISSION_ID);
        assertThat(analysisCaptor.getValue().getSubmissionId()).isEqualTo(SUBMISSION_ID);
        assertThat(analysisCaptor.getValue().getFindings()).hasSize(1);
    }
}
