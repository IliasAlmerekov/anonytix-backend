package de.anonytix.survey;

import java.util.UUID;

public record SurveyDescriptor(
        UUID id,
        UUID companyId,
        String title,
        String type,
        String status) {

    public boolean published() {
        return "PUBLISHED".equals(status);
    }
}
