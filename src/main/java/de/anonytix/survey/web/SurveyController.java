package de.anonytix.survey.web;

import de.anonytix.survey.dto.CreateSurveyRequest;
import de.anonytix.survey.dto.QuestionResponse;
import de.anonytix.survey.dto.QuestionWriteRequest;
import de.anonytix.survey.dto.ReorderQuestionsRequest;
import de.anonytix.survey.dto.SurveyResponse;
import de.anonytix.survey.dto.SurveySummaryResponse;
import de.anonytix.survey.dto.UpdateSurveyRequest;
import de.anonytix.survey.service.SurveyService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/surveys")
public class SurveyController {

    private final SurveyService surveyService;

    public SurveyController(SurveyService surveyService) {
        this.surveyService = surveyService;
    }

    @GetMapping
    List<SurveySummaryResponse> list(@PathVariable UUID companyId) {
        return surveyService.list(companyId);
    }

    @PostMapping
    ResponseEntity<SurveyResponse> create(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateSurveyRequest request) {
        SurveyResponse created = surveyService.create(companyId, request);
        return ResponseEntity.created(URI.create(
                        "/api/v1/companies/%s/surveys/%s".formatted(companyId, created.id())))
                .body(created);
    }

    @GetMapping("/{surveyId}")
    SurveyResponse get(@PathVariable UUID companyId, @PathVariable UUID surveyId) {
        return surveyService.get(companyId, surveyId);
    }

    @PatchMapping("/{surveyId}")
    SurveyResponse update(
            @PathVariable UUID companyId,
            @PathVariable UUID surveyId,
            @Valid @RequestBody UpdateSurveyRequest request) {
        return surveyService.update(companyId, surveyId, request);
    }

    @PostMapping("/{surveyId}/questions")
    ResponseEntity<QuestionResponse> addQuestion(
            @PathVariable UUID companyId,
            @PathVariable UUID surveyId,
            @Valid @RequestBody QuestionWriteRequest request) {
        QuestionResponse created = surveyService.addQuestion(companyId, surveyId, request);
        return ResponseEntity.created(URI.create(
                        "/api/v1/companies/%s/surveys/%s/questions/%s"
                                .formatted(companyId, surveyId, created.id())))
                .body(created);
    }

    @PatchMapping("/{surveyId}/questions/{questionId}")
    QuestionResponse updateQuestion(
            @PathVariable UUID companyId,
            @PathVariable UUID surveyId,
            @PathVariable UUID questionId,
            @Valid @RequestBody QuestionWriteRequest request) {
        return surveyService.updateQuestion(companyId, surveyId, questionId, request);
    }

    @DeleteMapping("/{surveyId}/questions/{questionId}")
    ResponseEntity<Void> deactivateQuestion(
            @PathVariable UUID companyId,
            @PathVariable UUID surveyId,
            @PathVariable UUID questionId) {
        surveyService.deactivateQuestion(companyId, surveyId, questionId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{surveyId}/questions/order")
    ResponseEntity<Void> reorder(
            @PathVariable UUID companyId,
            @PathVariable UUID surveyId,
            @Valid @RequestBody ReorderQuestionsRequest request) {
        surveyService.reorder(companyId, surveyId, request);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{surveyId}/publish")
    SurveyResponse publish(@PathVariable UUID companyId, @PathVariable UUID surveyId) {
        return surveyService.publish(companyId, surveyId);
    }
}
