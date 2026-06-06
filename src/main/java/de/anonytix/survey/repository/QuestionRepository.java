package de.anonytix.survey.repository;

import de.anonytix.survey.domain.Question;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuestionRepository extends JpaRepository<Question, UUID> {

    Optional<Question> findByIdAndSurveyId(UUID id, UUID surveyId);
}
