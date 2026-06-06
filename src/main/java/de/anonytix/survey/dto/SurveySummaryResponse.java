package de.anonytix.survey.dto;

import de.anonytix.survey.domain.SurveyStatus;
import de.anonytix.survey.domain.SurveyType;
import java.util.UUID;

public record SurveySummaryResponse(
        UUID id,
        String title,
        SurveyType type,
        SurveyStatus status,
        int version) {
}
