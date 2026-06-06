package de.anonytix.analysis.domain;

import de.anonytix.analysis.gateway.AnalysisResult;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "ai_analyses")
public class AiAnalysis {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "submission_id", nullable = false, unique = true)
    private UUID submissionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "overall_sentiment", nullable = false, length = 20)
    private Sentiment overallSentiment;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(name = "pii_detected", nullable = false)
    private boolean piiDetected;

    @Enumerated(EnumType.STRING)
    @Column(name = "risk_level", nullable = false, length = 20)
    private RiskLevel riskLevel;

    @Column(nullable = false, length = 120)
    private String model;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @OneToMany(mappedBy = "analysis", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<AnalysisFinding> findings = new ArrayList<>();

    protected AiAnalysis() {
    }

    public AiAnalysis(UUID submissionId, AnalysisResult result) {
        this.submissionId = submissionId;
        this.overallSentiment = Sentiment.valueOf(result.overallSentiment());
        this.summary = result.summary();
        this.piiDetected = result.piiDetected();
        this.riskLevel = RiskLevel.valueOf(result.riskLevel());
        this.model = result.model();
        this.createdAt = Instant.now();
        result.findings().forEach(finding ->
                findings.add(new AnalysisFinding(this, finding)));
    }

    public UUID getSubmissionId() {
        return submissionId;
    }

    public Sentiment getOverallSentiment() {
        return overallSentiment;
    }

    public String getSummary() {
        return summary;
    }

    public boolean isPiiDetected() {
        return piiDetected;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public String getModel() {
        return model;
    }

    public List<AnalysisFinding> getFindings() {
        return List.copyOf(findings);
    }
}
