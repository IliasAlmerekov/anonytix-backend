package de.anonytix.survey.service;

import de.anonytix.survey.domain.Question;
import de.anonytix.survey.domain.QuestionOption;
import de.anonytix.survey.domain.QuestionSource;
import de.anonytix.survey.domain.QuestionType;
import de.anonytix.survey.domain.Survey;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SurveyTemplateService {

    public void apply(String templateKey, Survey survey) {
        if (!List.of("EMPLOYEE_SATISFACTION", "EXIT_INTERVIEW").contains(templateKey)) {
            throw new IllegalArgumentException("Unbekannte Umfragevorlage.");
        }

        survey.addQuestion(rating(survey, 1, "Wie zufrieden bist du insgesamt mit deiner Arbeit?",
                "OVERALL_SATISFACTION", true));
        survey.addQuestion(rating(survey, 2, "Wie bewertest du die Kommunikation deiner Führungskraft?",
                "LEADERSHIP", true));

        Question workload = new Question(
                survey,
                "Wie empfindest du deine aktuelle Arbeitsbelastung?",
                null,
                QuestionType.SINGLE_CHOICE,
                "WORKLOAD",
                QuestionSource.STANDARD,
                true,
                3,
                null,
                null,
                null,
                false);
        workload.replaceOptions(List.of(
                new QuestionOption(workload, "Zu niedrig", "TOO_LOW", 1),
                new QuestionOption(workload, "Angemessen", "BALANCED", 2),
                new QuestionOption(workload, "Zu hoch", "TOO_HIGH", 3)));
        survey.addQuestion(workload);

        survey.addQuestion(rating(survey, 4, "Wie gut funktioniert die interne Kommunikation?",
                "COMMUNICATION", true));
        survey.addQuestion(rating(survey, 5, "Würdest du das Unternehmen weiterempfehlen?",
                "RECOMMENDATION", true));
        survey.addQuestion(text(survey, 6, "Was funktioniert aktuell besonders gut?",
                "POSITIVE_FEEDBACK"));
        survey.addQuestion(text(survey, 7, "Was sollte das Unternehmen verbessern?",
                "IMPROVEMENT"));
    }

    private Question rating(
            Survey survey,
            int position,
            String text,
            String category,
            boolean required) {
        return new Question(
                survey,
                text,
                null,
                QuestionType.RATING,
                category,
                QuestionSource.STANDARD,
                required,
                position,
                1,
                5,
                null,
                false);
    }

    private Question text(Survey survey, int position, String text, String category) {
        return new Question(
                survey,
                text,
                "Bitte nenne keine Namen oder andere identifizierende Informationen.",
                QuestionType.TEXT,
                category,
                QuestionSource.STANDARD,
                false,
                position,
                null,
                null,
                2000,
                true);
    }
}
