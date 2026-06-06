package de.anonytix.survey.mapper;

import de.anonytix.survey.domain.Question;
import de.anonytix.survey.domain.QuestionOption;
import de.anonytix.survey.domain.Survey;
import de.anonytix.survey.dto.QuestionOptionResponse;
import de.anonytix.survey.dto.QuestionResponse;
import de.anonytix.survey.dto.SurveyResponse;
import de.anonytix.survey.dto.SurveySummaryResponse;
import java.util.Comparator;
import org.springframework.stereotype.Component;

@Component
public class SurveyMapper {

    public SurveySummaryResponse toSummary(Survey survey) {
        return new SurveySummaryResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getType(),
                survey.getStatus(),
                1);
    }

    public SurveyResponse toResponse(Survey survey) {
        return new SurveyResponse(
                survey.getId(),
                survey.getTitle(),
                survey.getType(),
                survey.getStatus(),
                1,
                survey.getDescription(),
                survey.getQuestions().stream()
                        .filter(Question::isActive)
                        .sorted(Comparator.comparingInt(Question::getPosition))
                        .map(this::toQuestion)
                        .toList());
    }

    public QuestionResponse toQuestion(Question question) {
        return new QuestionResponse(
                question.getId(),
                question.getText(),
                question.getHelpText(),
                question.getType(),
                question.getCategory(),
                question.isRequired(),
                question.getPosition(),
                question.getMinimumValue(),
                question.getMaximumValue(),
                question.getMaximumLength(),
                question.isAnalyzeWithAi(),
                question.getDepartmentIds(),
                question.getOptions().stream()
                        .sorted(Comparator.comparingInt(QuestionOption::getPosition))
                        .map(option -> new QuestionOptionResponse(
                                option.getId(),
                                option.getLabel(),
                                option.getValue(),
                                option.getPosition()))
                        .toList());
    }
}
