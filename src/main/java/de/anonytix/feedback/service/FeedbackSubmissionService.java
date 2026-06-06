package de.anonytix.feedback.service;

import de.anonytix.campaign.InvitationAccess;
import de.anonytix.campaign.InvitationDescriptor;
import de.anonytix.company.CompanyDirectory;
import de.anonytix.feedback.domain.Answer;
import de.anonytix.feedback.domain.FeedbackSubmission;
import de.anonytix.feedback.FeedbackSubmitted;
import de.anonytix.feedback.dto.AnswerRequest;
import de.anonytix.feedback.dto.SubmissionResponse;
import de.anonytix.feedback.dto.SubmitFeedbackRequest;
import de.anonytix.feedback.repository.FeedbackSubmissionRepository;
import de.anonytix.survey.SurveyDirectory;
import de.anonytix.survey.SurveyFormDescriptor;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackSubmissionService {

    private final InvitationAccess invitationAccess;
    private final CompanyDirectory companyDirectory;
    private final SurveyDirectory surveyDirectory;
    private final FeedbackSubmissionRepository submissionRepository;
    private final ApplicationEventPublisher eventPublisher;

    public FeedbackSubmissionService(
            InvitationAccess invitationAccess,
            CompanyDirectory companyDirectory,
            SurveyDirectory surveyDirectory,
            FeedbackSubmissionRepository submissionRepository,
            ApplicationEventPublisher eventPublisher) {
        this.invitationAccess = invitationAccess;
        this.companyDirectory = companyDirectory;
        this.surveyDirectory = surveyDirectory;
        this.submissionRepository = submissionRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public SubmissionResponse submit(String token, SubmitFeedbackRequest request) {
        InvitationDescriptor invitation = invitationAccess.lock(token);
        if (!companyDirectory.departmentsBelongToCompany(
                invitation.companyId(), Set.of(request.departmentId()))) {
            throw new IllegalArgumentException("Die ausgewählte Abteilung gehört nicht zur Firma.");
        }
        SurveyFormDescriptor survey = surveyDirectory
                .findForm(invitation.companyId(), invitation.surveyId())
                .orElseThrow();
        Map<UUID, SurveyFormDescriptor.FormQuestion> visibleQuestions = new HashMap<>();
        survey.questions().stream()
                .filter(question -> question.departmentIds().isEmpty()
                        || question.departmentIds().contains(request.departmentId()))
                .forEach(question -> visibleQuestions.put(question.id(), question));
        validateAnswers(request, visibleQuestions);

        FeedbackSubmission submission = new FeedbackSubmission(
                invitation.companyId(),
                invitation.campaignId(),
                request.departmentId());
        for (AnswerRequest answerRequest : request.answers()) {
            submission.addAnswer(new Answer(
                    submission,
                    answerRequest.questionId(),
                    answerRequest.numericValue(),
                    answerRequest.textValue(),
                    answerRequest.booleanValue(),
                    Set.copyOf(answerRequest.selectedOptionIds())));
        }
        FeedbackSubmission saved = submissionRepository.save(submission);
        invitationAccess.markUsed(invitation.tokenHash());
        eventPublisher.publishEvent(new FeedbackSubmitted(saved.getId()));
        return new SubmissionResponse(
                saved.getId(),
                saved.getStatus(),
                saved.getSubmittedAt(),
                "Vielen Dank. Dein Feedback wurde erfolgreich und anonym übermittelt.");
    }

    private void validateAnswers(
            SubmitFeedbackRequest request,
            Map<UUID, SurveyFormDescriptor.FormQuestion> visibleQuestions) {
        Set<UUID> answeredIds = new HashSet<>();
        for (AnswerRequest answer : request.answers()) {
            SurveyFormDescriptor.FormQuestion question = visibleQuestions.get(answer.questionId());
            if (question == null) {
                throw new IllegalArgumentException(
                        "Eine Antwort gehört nicht zum sichtbaren Formular.");
            }
            if (!answeredIds.add(answer.questionId())) {
                throw new IllegalArgumentException("Eine Frage wurde mehrfach beantwortet.");
            }
            validateValue(question, answer);
        }
        boolean missingRequired = visibleQuestions.values().stream()
                .filter(SurveyFormDescriptor.FormQuestion::required)
                .anyMatch(question -> !answeredIds.contains(question.id()));
        if (missingRequired) {
            throw new IllegalArgumentException("Mindestens eine Pflichtfrage wurde nicht beantwortet.");
        }
    }

    private void validateValue(
            SurveyFormDescriptor.FormQuestion question,
            AnswerRequest answer) {
        switch (question.type()) {
            case "RATING" -> validateRating(question, answer);
            case "TEXT" -> validateText(question, answer);
            case "BOOLEAN" -> {
                if (answer.booleanValue() == null) {
                    throw new IllegalArgumentException("Boolean-Fragen benötigen einen Wert.");
                }
            }
            case "SINGLE_CHOICE" -> validateChoice(question, answer, true);
            case "MULTI_CHOICE" -> validateChoice(question, answer, false);
            default -> throw new IllegalArgumentException("Unbekannter Fragetyp.");
        }
    }

    private void validateRating(
            SurveyFormDescriptor.FormQuestion question,
            AnswerRequest answer) {
        BigDecimal value = answer.numericValue();
        if (value == null
                || value.compareTo(BigDecimal.valueOf(question.minimumValue())) < 0
                || value.compareTo(BigDecimal.valueOf(question.maximumValue())) > 0) {
            throw new IllegalArgumentException("Bewertung liegt außerhalb des erlaubten Bereichs.");
        }
    }

    private void validateText(
            SurveyFormDescriptor.FormQuestion question,
            AnswerRequest answer) {
        if (answer.textValue() == null && question.required()) {
            throw new IllegalArgumentException("Textfrage benötigt einen Wert.");
        }
        if (answer.textValue() != null
                && question.maximumLength() != null
                && answer.textValue().length() > question.maximumLength()) {
            throw new IllegalArgumentException("Textantwort ist zu lang.");
        }
    }

    private void validateChoice(
            SurveyFormDescriptor.FormQuestion question,
            AnswerRequest answer,
            boolean single) {
        if (answer.selectedOptionIds().isEmpty()
                || single && answer.selectedOptionIds().size() != 1) {
            throw new IllegalArgumentException("Auswahlfrage benötigt eine gültige Auswahl.");
        }
        Set<UUID> allowed = question.options().stream()
                .map(SurveyFormDescriptor.FormOption::id)
                .collect(java.util.stream.Collectors.toSet());
        if (!allowed.containsAll(answer.selectedOptionIds())) {
            throw new IllegalArgumentException("Eine ausgewählte Option gehört nicht zur Frage.");
        }
    }
}
