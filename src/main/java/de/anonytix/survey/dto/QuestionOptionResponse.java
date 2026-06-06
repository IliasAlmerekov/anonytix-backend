package de.anonytix.survey.dto;

import java.util.UUID;

public record QuestionOptionResponse(UUID id, String label, String value, int position) {
}
