package de.anonytix.analysis.domain;

import de.anonytix.analysis.gateway.AnalysisFindingResult;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "analysis_findings")
public class AnalysisFinding {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "analysis_id", nullable = false)
    private AiAnalysis analysis;

    @Column(nullable = false, length = 80)
    private String category;

    @Column(nullable = false, length = 300)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Sentiment sentiment;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FindingPriority priority;

    @Column(precision = 5, scale = 2)
    private BigDecimal score;

    @Column(name = "anonymized_text", columnDefinition = "TEXT")
    private String anonymizedText;

    protected AnalysisFinding() {
    }

    AnalysisFinding(AiAnalysis analysis, AnalysisFindingResult result) {
        this.analysis = analysis;
        this.category = result.category();
        this.label = result.label();
        this.sentiment = Sentiment.valueOf(result.sentiment());
        this.priority = FindingPriority.valueOf(result.priority());
        this.score = BigDecimal.valueOf(result.score());
        this.anonymizedText = result.anonymizedText();
    }

    public String getCategory() {
        return category;
    }

    public String getLabel() {
        return label;
    }

    public Sentiment getSentiment() {
        return sentiment;
    }

    public FindingPriority getPriority() {
        return priority;
    }

    public BigDecimal getScore() {
        return score;
    }

    public String getAnonymizedText() {
        return anonymizedText;
    }
}
