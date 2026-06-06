package de.anonytix.feedback.dto;

import de.anonytix.company.DepartmentReference;
import de.anonytix.survey.SurveyFormDescriptor.FormQuestion;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record PublicFormResponse(
        UUID campaignId,
        UUID surveyId,
        String title,
        String description,
        String surveyType,
        List<DepartmentReference> departments,
        UUID selectedDepartmentId,
        Instant expiresAt,
        List<FormQuestion> questions) {
}
