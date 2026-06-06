package de.anonytix.analysis.repository;

import de.anonytix.analysis.domain.AiAnalysis;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AiAnalysisRepository extends JpaRepository<AiAnalysis, UUID> {

    @EntityGraph(attributePaths = "findings")
    Optional<AiAnalysis> findBySubmissionId(UUID submissionId);
}
