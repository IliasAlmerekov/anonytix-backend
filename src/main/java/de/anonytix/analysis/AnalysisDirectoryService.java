package de.anonytix.analysis;

import de.anonytix.analysis.domain.AiAnalysis;
import de.anonytix.analysis.repository.AiAnalysisRepository;
import de.anonytix.shared.error.ResourceNotFoundException;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class AnalysisDirectoryService implements AnalysisDirectory {

    private final AiAnalysisRepository repository;

    AnalysisDirectoryService(AiAnalysisRepository repository) {
        this.repository = repository;
    }

    @Override
    @Transactional(readOnly = true)
    public AnalysisDescriptor getBySubmissionId(UUID submissionId) {
        AiAnalysis analysis = repository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Analyse für Feedback-Abgabe "
                                + submissionId
                                + " wurde nicht gefunden."));
        return new AnalysisDescriptor(
                analysis.getSubmissionId(),
                analysis.getOverallSentiment().name(),
                analysis.getSummary(),
                analysis.isPiiDetected(),
                analysis.getRiskLevel().name(),
                analysis.getModel(),
                analysis.getFindings().stream()
                        .map(finding -> new AnalysisFindingDescriptor(
                                finding.getCategory(),
                                finding.getLabel(),
                                finding.getSentiment().name(),
                                finding.getPriority().name(),
                                finding.getScore(),
                                finding.getAnonymizedText()))
                        .toList());
    }
}
