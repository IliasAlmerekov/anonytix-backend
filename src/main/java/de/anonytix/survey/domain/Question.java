package de.anonytix.survey.domain;

import de.anonytix.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "questions")
public class Question extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "survey_id", nullable = false)
    private Survey survey;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "help_text", length = 1000)
    private String helpText;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private QuestionType type;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private QuestionSource source;

    @Column(nullable = false)
    private boolean required;

    @Column(nullable = false)
    private int position;

    @Column(name = "minimum_value")
    private Integer minimumValue;

    @Column(name = "maximum_value")
    private Integer maximumValue;

    @Column(name = "maximum_length")
    private Integer maximumLength;

    @Column(name = "analyze_with_ai", nullable = false)
    private boolean analyzeWithAi;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private final Set<QuestionOption> options = new LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(
            name = "question_departments",
            joinColumns = @JoinColumn(name = "question_id"))
    @Column(name = "department_id")
    private final Set<UUID> departmentIds = new LinkedHashSet<>();

    protected Question() {
    }

    public Question(
            Survey survey,
            String text,
            String helpText,
            QuestionType type,
            String category,
            QuestionSource source,
            boolean required,
            int position,
            Integer minimumValue,
            Integer maximumValue,
            Integer maximumLength,
            boolean analyzeWithAi) {
        this.survey = survey;
        update(
                text,
                helpText,
                type,
                category,
                required,
                minimumValue,
                maximumValue,
                maximumLength,
                analyzeWithAi);
        this.source = source;
        this.position = position;
    }

    public void update(
            String text,
            String helpText,
            QuestionType type,
            String category,
            boolean required,
            Integer minimumValue,
            Integer maximumValue,
            Integer maximumLength,
            boolean analyzeWithAi) {
        this.text = text;
        this.helpText = helpText;
        this.type = type;
        this.category = category;
        this.required = required;
        this.minimumValue = minimumValue;
        this.maximumValue = maximumValue;
        this.maximumLength = maximumLength;
        this.analyzeWithAi = analyzeWithAi;
    }

    public void replaceOptions(List<QuestionOption> replacements) {
        options.clear();
        options.addAll(replacements);
    }

    public void replaceDepartments(Set<UUID> replacements) {
        departmentIds.clear();
        departmentIds.addAll(replacements);
    }

    public void moveTo(int position) {
        this.position = position;
    }

    public void deactivate() {
        active = false;
    }

    public Survey getSurvey() {
        return survey;
    }

    public String getText() {
        return text;
    }

    public String getHelpText() {
        return helpText;
    }

    public QuestionType getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public QuestionSource getSource() {
        return source;
    }

    public boolean isRequired() {
        return required;
    }

    public int getPosition() {
        return position;
    }

    public Integer getMinimumValue() {
        return minimumValue;
    }

    public Integer getMaximumValue() {
        return maximumValue;
    }

    public Integer getMaximumLength() {
        return maximumLength;
    }

    public boolean isAnalyzeWithAi() {
        return analyzeWithAi;
    }

    public boolean isActive() {
        return active;
    }

    public Set<QuestionOption> getOptions() {
        return options;
    }

    public Set<UUID> getDepartmentIds() {
        return departmentIds;
    }
}
