package de.anonytix.survey.service;

import de.anonytix.company.CompanyDirectory;
import de.anonytix.shared.error.ConflictException;
import de.anonytix.shared.error.ResourceNotFoundException;
import de.anonytix.survey.domain.Question;
import de.anonytix.survey.domain.QuestionOption;
import de.anonytix.survey.domain.QuestionSource;
import de.anonytix.survey.domain.QuestionType;
import de.anonytix.survey.domain.Survey;
import de.anonytix.survey.domain.SurveyStatus;
import de.anonytix.survey.dto.CreateSurveyRequest;
import de.anonytix.survey.dto.QuestionResponse;
import de.anonytix.survey.dto.QuestionWriteRequest;
import de.anonytix.survey.dto.ReorderQuestionsRequest;
import de.anonytix.survey.dto.SurveyResponse;
import de.anonytix.survey.dto.SurveySummaryResponse;
import de.anonytix.survey.dto.UpdateSurveyRequest;
import de.anonytix.survey.mapper.SurveyMapper;
import de.anonytix.survey.repository.QuestionRepository;
import de.anonytix.survey.repository.SurveyRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SurveyService {

    private final CompanyDirectory companyDirectory;
    private final SurveyRepository surveyRepository;
    private final QuestionRepository questionRepository;
    private final SurveyTemplateService templateService;
    private final SurveyMapper mapper;

    public SurveyService(
            CompanyDirectory companyDirectory,
            SurveyRepository surveyRepository,
            QuestionRepository questionRepository,
            SurveyTemplateService templateService,
            SurveyMapper mapper) {
        this.companyDirectory = companyDirectory;
        this.surveyRepository = surveyRepository;
        this.questionRepository = questionRepository;
        this.templateService = templateService;
        this.mapper = mapper;
    }

    public List<SurveySummaryResponse> list(UUID companyId) {
        requireCompany(companyId);
        return surveyRepository.findAllByCompanyIdOrderByCreatedAtDesc(companyId).stream()
                .map(mapper::toSummary)
                .toList();
    }

    public SurveyResponse get(UUID companyId, UUID surveyId) {
        return mapper.toResponse(requireDetailedSurvey(companyId, surveyId));
    }

    @Transactional
    public SurveyResponse create(UUID companyId, CreateSurveyRequest request) {
        requireCompany(companyId);
        Survey survey = new Survey(
                companyId,
                request.title().trim(),
                request.description(),
                request.type(),
                request.templateKey());
        templateService.apply(request.templateKey(), survey);
        return mapper.toResponse(surveyRepository.save(survey));
    }

    @Transactional
    public SurveyResponse update(
            UUID companyId,
            UUID surveyId,
            UpdateSurveyRequest request) {
        Survey survey = requireSurvey(companyId, surveyId);
        requireDraft(survey);
        survey.update(
                request.title() == null ? null : request.title().trim(),
                request.description());
        return mapper.toResponse(survey);
    }

    @Transactional
    public QuestionResponse addQuestion(
            UUID companyId,
            UUID surveyId,
            QuestionWriteRequest request) {
        Survey survey = requireDetailedSurvey(companyId, surveyId);
        requireDraft(survey);
        validateRequest(companyId, request);
        int position = survey.getQuestions().stream()
                .mapToInt(Question::getPosition)
                .max()
                .orElse(0) + 1;
        Question question = createQuestion(survey, request, QuestionSource.CUSTOM, position);
        survey.addQuestion(question);
        return mapper.toQuestion(questionRepository.save(question));
    }

    @Transactional
    public QuestionResponse updateQuestion(
            UUID companyId,
            UUID surveyId,
            UUID questionId,
            QuestionWriteRequest request) {
        Survey survey = requireSurvey(companyId, surveyId);
        requireDraft(survey);
        validateRequest(companyId, request);
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Frage wurde nicht gefunden."));
        apply(question, request);
        return mapper.toQuestion(question);
    }

    @Transactional
    public void deactivateQuestion(UUID companyId, UUID surveyId, UUID questionId) {
        Survey survey = requireSurvey(companyId, surveyId);
        requireDraft(survey);
        Question question = questionRepository.findByIdAndSurveyId(questionId, surveyId)
                .orElseThrow(() -> new ResourceNotFoundException("Frage wurde nicht gefunden."));
        question.deactivate();
    }

    @Transactional
    public void reorder(
            UUID companyId,
            UUID surveyId,
            ReorderQuestionsRequest request) {
        Survey survey = requireDetailedSurvey(companyId, surveyId);
        requireDraft(survey);
        List<Question> active = survey.getQuestions().stream()
                .filter(Question::isActive)
                .toList();
        Set<UUID> expected = active.stream().map(Question::getId).collect(java.util.stream.Collectors.toSet());
        if (!expected.equals(new HashSet<>(request.questionIds()))
                || request.questionIds().size() != active.size()) {
            throw new IllegalArgumentException("Die Fragenliste ist unvollständig oder enthält fremde Fragen.");
        }
        for (int index = 0; index < request.questionIds().size(); index++) {
            UUID questionId = request.questionIds().get(index);
            active.stream()
                    .filter(question -> question.getId().equals(questionId))
                    .findFirst()
                    .orElseThrow()
                    .moveTo(index + 1);
        }
    }

    @Transactional
    public SurveyResponse publish(UUID companyId, UUID surveyId) {
        Survey survey = requireDetailedSurvey(companyId, surveyId);
        requireDraft(survey);
        if (survey.getQuestions().stream().noneMatch(Question::isActive)) {
            throw new IllegalArgumentException("Eine Umfrage benötigt mindestens eine aktive Frage.");
        }
        survey.publish();
        return mapper.toResponse(survey);
    }

    private Question createQuestion(
            Survey survey,
            QuestionWriteRequest request,
            QuestionSource source,
            int position) {
        Question question = new Question(
                survey,
                request.text().trim(),
                request.helpText(),
                request.type(),
                request.category().trim(),
                source,
                request.required(),
                position,
                request.minimumValue(),
                request.maximumValue(),
                request.maximumLength(),
                request.analyzeWithAi());
        replaceRelations(question, request);
        return question;
    }

    private void apply(Question question, QuestionWriteRequest request) {
        question.update(
                request.text().trim(),
                request.helpText(),
                request.type(),
                request.category().trim(),
                request.required(),
                request.minimumValue(),
                request.maximumValue(),
                request.maximumLength(),
                request.analyzeWithAi());
        replaceRelations(question, request);
    }

    private void replaceRelations(Question question, QuestionWriteRequest request) {
        question.replaceDepartments(Set.copyOf(request.departmentIds()));
        question.replaceOptions(request.options().stream()
                .map(option -> new QuestionOption(
                        question,
                        option.label().trim(),
                        option.value().trim(),
                        option.position()))
                .toList());
    }

    private void validateRequest(UUID companyId, QuestionWriteRequest request) {
        if (!companyDirectory.departmentsBelongToCompany(companyId, request.departmentIds())) {
            throw new IllegalArgumentException("Mindestens eine Abteilung gehört nicht zur Firma.");
        }
        if (request.type() == QuestionType.RATING
                && (request.minimumValue() == null
                || request.maximumValue() == null
                || request.minimumValue() >= request.maximumValue())) {
            throw new IllegalArgumentException("Bewertungsfragen benötigen einen gültigen Wertebereich.");
        }
        if (Set.of(QuestionType.SINGLE_CHOICE, QuestionType.MULTI_CHOICE).contains(request.type())
                && request.options().isEmpty()) {
            throw new IllegalArgumentException("Auswahlfragen benötigen Optionen.");
        }
        if (request.type() == QuestionType.TEXT
                && request.maximumLength() != null
                && request.maximumLength() < 1) {
            throw new IllegalArgumentException("Die maximale Textlänge muss positiv sein.");
        }
    }

    private Survey requireDetailedSurvey(UUID companyId, UUID surveyId) {
        return surveyRepository.findDetailedByIdAndCompanyId(surveyId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Umfrage wurde nicht gefunden."));
    }

    private Survey requireSurvey(UUID companyId, UUID surveyId) {
        return surveyRepository.findByIdAndCompanyId(surveyId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Umfrage wurde nicht gefunden."));
    }

    private void requireCompany(UUID companyId) {
        if (!companyDirectory.companyExists(companyId)) {
            throw new ResourceNotFoundException("Firma wurde nicht gefunden.");
        }
    }

    private void requireDraft(Survey survey) {
        if (survey.getStatus() != SurveyStatus.DRAFT) {
            throw new ConflictException(
                    "SURVEY_ALREADY_PUBLISHED",
                    "Veröffentlichte Umfragen können nicht mehr verändert werden.");
        }
    }
}
