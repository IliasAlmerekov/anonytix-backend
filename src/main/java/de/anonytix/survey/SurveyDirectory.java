package de.anonytix.survey;

import java.util.Optional;
import java.util.UUID;

public interface SurveyDirectory {

    Optional<SurveyDescriptor> find(UUID companyId, UUID surveyId);

    Optional<SurveyFormDescriptor> findForm(UUID companyId, UUID surveyId);
}
