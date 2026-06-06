package de.anonytix.survey.domain;

import de.anonytix.shared.domain.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "surveys")
public class Survey extends BaseEntity {

    @Column(name = "company_id", nullable = false)
    private UUID companyId;

    @Column(nullable = false, length = 240)
    private String title;

    @Column
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SurveyType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SurveyStatus status = SurveyStatus.DRAFT;

    @Column(name = "template_key", length = 80)
    private String templateKey;

    @Column(name = "published_at")
    private Instant publishedAt;

    @OneToMany(mappedBy = "survey", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private final Set<Question> questions = new LinkedHashSet<>();

    protected Survey() {
    }

    public Survey(
            UUID companyId,
            String title,
            String description,
            SurveyType type,
            String templateKey) {
        this.companyId = companyId;
        this.title = title;
        this.description = description;
        this.type = type;
        this.templateKey = templateKey;
    }

    public void addQuestion(Question question) {
        questions.add(question);
    }

    public void update(String title, String description) {
        if (title != null) {
            this.title = title;
        }
        if (description != null) {
            this.description = description;
        }
    }

    public void publish() {
        status = SurveyStatus.PUBLISHED;
        publishedAt = Instant.now();
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public SurveyType getType() {
        return type;
    }

    public SurveyStatus getStatus() {
        return status;
    }

    public String getTemplateKey() {
        return templateKey;
    }

    public Instant getPublishedAt() {
        return publishedAt;
    }

    public Set<Question> getQuestions() {
        return questions;
    }
}
