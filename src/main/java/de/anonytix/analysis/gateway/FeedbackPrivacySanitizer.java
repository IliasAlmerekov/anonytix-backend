package de.anonytix.analysis.gateway;

import de.anonytix.feedback.FeedbackAnalysisInput;
import de.anonytix.feedback.FeedbackAnswerData;
import java.util.List;
import java.util.regex.Pattern;

final class FeedbackPrivacySanitizer {

    private static final List<Replacement> REPLACEMENTS = List.of(
            new Replacement(
                    Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}\\b"),
                    "[E-MAIL ENTFERNT]"),
            new Replacement(
                    Pattern.compile("(?<!\\d)(?:\\+49|0)[\\d /()-]{7,}\\d"),
                    "[TELEFONNUMMER ENTFERNT]"),
            new Replacement(
                    Pattern.compile(
                            "(?iu)\\b(?:Herr|Frau|Dr\\.?|Prof\\.?)\\s+"
                                    + "[\\p{L}-]+(?:\\s+[\\p{L}-]+)?"),
                    "[PERSON ENTFERNT]"),
            new Replacement(
                    Pattern.compile("(?iu)\\bhttps?://\\S+|\\bwww\\.\\S+"),
                    "[LINK ENTFERNT]"),
            new Replacement(
                    Pattern.compile("(?<![\\w.])@[A-Za-z0-9_]{2,30}\\b"),
                    "[BENUTZERNAME ENTFERNT]"));

    SanitizedFeedback sanitize(FeedbackAnalysisInput input) {
        boolean piiDetected = false;
        List<FeedbackAnswerData> answers = input.answers().stream()
                .map(answer -> {
                    SanitizedText text = sanitizeText(answer.textValue());
                    return new FeedbackAnswerData(
                            answer.questionId(),
                            answer.numericValue(),
                            text.text(),
                            answer.booleanValue(),
                            answer.selectedOptionIds());
                })
                .toList();

        for (int index = 0; index < input.answers().size(); index++) {
            String original = input.answers().get(index).textValue();
            String sanitized = answers.get(index).textValue();
            if (original != null && !original.equals(sanitized)) {
                piiDetected = true;
                break;
            }
        }

        return new SanitizedFeedback(
                new FeedbackAnalysisInput(
                        input.submissionId(),
                        input.companyId(),
                        input.campaignId(),
                        input.departmentId(),
                        answers),
                piiDetected);
    }

    SanitizedText sanitizeText(String text) {
        if (text == null || text.isBlank()) {
            return new SanitizedText(text, false);
        }
        String sanitized = text;
        boolean piiDetected = false;
        for (Replacement replacement : REPLACEMENTS) {
            String replaced = replacement.pattern().matcher(sanitized)
                    .replaceAll(replacement.replacement());
            piiDetected = piiDetected || !replaced.equals(sanitized);
            sanitized = replaced;
        }
        return new SanitizedText(sanitized, piiDetected);
    }

    record SanitizedFeedback(
            FeedbackAnalysisInput input,
            boolean piiDetected) {
    }

    record SanitizedText(
            String text,
            boolean piiDetected) {
    }

    private record Replacement(
            Pattern pattern,
            String replacement) {
    }
}
