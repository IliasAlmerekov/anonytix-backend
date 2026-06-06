package de.anonytix.campaign.repository;

import de.anonytix.campaign.domain.Campaign;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CampaignRepository extends JpaRepository<Campaign, UUID> {

    List<Campaign> findAllByCompanyIdOrderByStartsAtDesc(UUID companyId);

    Optional<Campaign> findByIdAndCompanyId(UUID id, UUID companyId);
}
