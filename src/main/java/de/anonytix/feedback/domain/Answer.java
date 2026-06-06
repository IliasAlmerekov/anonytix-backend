package de.anonytix.feedback.domain;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "answers")
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submission_id", nullable = false)
    private FeedbackSubmission submission;

    @Column(name = "question_id", nullable = false)
    private UUID questionId;

    @Column(name = "numeric_value")
    private BigDecimal numericValue;

    @Column(name = "text_value")
    private String textValue;

    @Column(name = "boolean_value")
    private Boolean booleanValue;

    @ElementCollection
    @CollectionTable(
            name = "answer_selected_options",
            joinColumns = @JoinColumn(name = "answer_id"))
    @Column(name = "option_id")
    private final Set<UUID> selectedOptionIds = new LinkedHashSet<>();

    protected Answer() {
    }

    public Answer(
            FeedbackSubmission submission,
            UUID questionId,
            BigDecimal numericValue,
            String textValue,
            Boolean booleanValue,
            Set<UUID> selectedOptionIds) {
        this.submission = submission;
        this.questionId = questionId;
        this.numericValue = numericValue;
        this.textValue = textValue;
        this.booleanValue = booleanValue;
        this.selectedOptionIds.addAll(selectedOptionIds);
    }

    public UUID getQuestionId() {
        return questionId;
    }

    public BigDecimal getNumericValue() {
        return numericValue;
    }

    public String getTextValue() {
        return textValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }

    public Set<UUID> getSelectedOptionIds() {
        return Set.copyOf(selectedOptionIds);
    }
}
