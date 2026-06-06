package de.anonytix.survey.repository;

import de.anonytix.survey.domain.Survey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, UUID> {

    List<Survey> findAllByCompanyIdOrderByCreatedAtDesc(UUID companyId);

    @EntityGraph(attributePaths = {"questions", "questions.options", "questions.departmentIds"})
    Optional<Survey> findDetailedByIdAndCompanyId(UUID id, UUID companyId);

    Optional<Survey> findByIdAndCompanyId(UUID id, UUID companyId);
}
