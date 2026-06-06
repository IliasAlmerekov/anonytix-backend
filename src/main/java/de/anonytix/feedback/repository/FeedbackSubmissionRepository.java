package de.anonytix.feedback.repository;

import de.anonytix.feedback.domain.FeedbackSubmission;
import de.anonytix.feedback.domain.SubmissionStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FeedbackSubmissionRepository
        extends JpaRepository<FeedbackSubmission, UUID> {

    @EntityGraph(attributePaths = {"answers", "answers.selectedOptionIds"})
    @Query("select distinct submission from FeedbackSubmission submission where submission.id = :id")
    Optional<FeedbackSubmission> findForAnalysis(@Param("id") UUID id);

    List<FeedbackSubmission> findAllByStatusOrderBySubmittedAtAsc(SubmissionStatus status);
}
