package de.anonytix.feedback.service;

import de.anonytix.campaign.InvitationAccess;
import de.anonytix.campaign.InvitationDescriptor;
import de.anonytix.company.CompanyDirectory;
import de.anonytix.company.DepartmentReference;
import de.anonytix.feedback.dto.PublicFormResponse;
import de.anonytix.shared.error.ResourceNotFoundException;
import de.anonytix.survey.SurveyDirectory;
import de.anonytix.survey.SurveyFormDescriptor;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PublicFormService {

    private final InvitationAccess invitationAccess;
    private final CompanyDirectory companyDirectory;
    private final SurveyDirectory surveyDirectory;

    public PublicFormService(
            InvitationAccess invitationAccess,
            CompanyDirectory companyDirectory,
            SurveyDirectory surveyDirectory) {
        this.invitationAccess = invitationAccess;
        this.companyDirectory = companyDirectory;
        this.surveyDirectory = surveyDirectory;
    }

    public PublicFormResponse load(String token, UUID departmentId) {
        InvitationDescriptor invitation = invitationAccess.resolve(token);
        List<DepartmentReference> departments =
                companyDirectory.activeDepartments(invitation.companyId());
        if (departmentId != null
                && departments.stream().noneMatch(department -> department.id().equals(departmentId))) {
            throw new ResourceNotFoundException("Abteilung wurde nicht gefunden.");
        }
        SurveyFormDescriptor survey = surveyDirectory
                .findForm(invitation.companyId(), invitation.surveyId())
                .orElseThrow(() -> new ResourceNotFoundException("Umfrage wurde nicht gefunden."));
        List<SurveyFormDescriptor.FormQuestion> questions = survey.questions().stream()
                .filter(question -> question.departmentIds().isEmpty()
                        || departmentId != null && question.departmentIds().contains(departmentId))
                .sorted(Comparator.comparingInt(SurveyFormDescriptor.FormQuestion::position))
                .toList();
        return new PublicFormResponse(
                invitation.campaignId(),
                survey.id(),
                invitation.campaignName(),
                survey.description(),
                survey.type(),
                departments,
                departmentId,
                invitation.expiresAt(),
                questions);
    }
}
