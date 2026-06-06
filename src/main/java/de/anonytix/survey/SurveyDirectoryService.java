package de.anonytix.survey;

import de.anonytix.survey.repository.SurveyRepository;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
class SurveyDirectoryService implements SurveyDirectory {

    private final SurveyRepository surveyRepository;

    SurveyDirectoryService(SurveyRepository surveyRepository) {
        this.surveyRepository = surveyRepository;
    }

    @Override
    public Optional<SurveyDescriptor> find(UUID companyId, UUID surveyId) {
        return surveyRepository.findByIdAndCompanyId(surveyId, companyId)
                .map(survey -> new SurveyDescriptor(
                        survey.getId(),
                        survey.getCompanyId(),
                        survey.getTitle(),
                        survey.getType().name(),
                        survey.getStatus().name()));
    }

    @Override
    public Optional<SurveyFormDescriptor> findForm(UUID companyId, UUID surveyId) {
        return surveyRepository.findDetailedByIdAndCompanyId(surveyId, companyId)
                .map(survey -> new SurveyFormDescriptor(
                        survey.getId(),
                        survey.getTitle(),
                        survey.getDescription(),
                        survey.getType().name(),
                        survey.getQuestions().stream()
                                .filter(question -> question.isActive())
                                .map(question -> new SurveyFormDescriptor.FormQuestion(
                                        question.getId(),
                                        question.getText(),
                                        question.getHelpText(),
                                        question.getType().name(),
                                        question.getCategory(),
                                        question.isRequired(),
                                        question.getPosition(),
                                        question.getMinimumValue(),
                                        question.getMaximumValue(),
                                        question.getMaximumLength(),
                                        question.isAnalyzeWithAi(),
                                        Set.copyOf(question.getDepartmentIds()),
                                        question.getOptions().stream()
                                                .map(option -> new SurveyFormDescriptor.FormOption(
                                                        option.getId(),
                                                        option.getLabel(),
                                                        option.getValue(),
                                                        option.getPosition()))
                                                .toList()))
                                .toList()));
    }
}
