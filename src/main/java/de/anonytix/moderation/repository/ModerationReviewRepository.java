package de.anonytix.moderation.repository;

import de.anonytix.moderation.domain.ModerationReview;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ModerationReviewRepository
        extends JpaRepository<ModerationReview, UUID> {
}
