package de.anonytix.survey;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public record SurveyFormDescriptor(
        UUID id,
        String title,
        String description,
        String type,
        List<FormQuestion> questions) {

    public record FormQuestion(
            UUID id,
            String text,
            String helpText,
            String type,
            String category,
            boolean required,
            int position,
            Integer minimumValue,
            Integer maximumValue,
            Integer maximumLength,
            boolean analyzeWithAi,
            Set<UUID> departmentIds,
            List<FormOption> options) {
    }

    public record FormOption(UUID id, String label, String value, int position) {
    }
}
