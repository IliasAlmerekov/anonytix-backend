package de.anonytix.campaign.mapper;

import de.anonytix.campaign.domain.Campaign;
import de.anonytix.campaign.dto.CampaignResponse;
import de.anonytix.shared.mapper.MappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class)
public interface CampaignMapper {

    CampaignResponse toResponse(Campaign campaign);
}
