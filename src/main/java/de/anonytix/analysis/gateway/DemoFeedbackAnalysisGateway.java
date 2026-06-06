package de.anonytix.analysis.gateway;

import de.anonytix.feedback.FeedbackAnalysisInput;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "anonytix.ai.provider",
        havingValue = "demo",
        matchIfMissing = true)
public class DemoFeedbackAnalysisGateway implements FeedbackAnalysisGateway {

    private static final Pattern EMAIL =
            Pattern.compile("\\b[\\w.%+-]+@[\\w.-]+\\.[A-Za-z]{2,}\\b");
    private static final Pattern PHONE =
            Pattern.compile("(?<!\\d)(?:\\+49|0)[\\d /()-]{7,}\\d");
    private static final Pattern PERSON =
            Pattern.compile("(?iu)\\b(?:Herr|Frau)\\s+[\\p{L}-]+(?:\\s+[\\p{L}-]+)?");
    private static final Map<String, Topic> TOPICS = topics();

    @Override
    public AnalysisResult analyze(FeedbackAnalysisInput input) {
        String originalText = input.answers().stream()
                .map(answer -> answer.textValue() == null ? "" : answer.textValue().trim())
                .filter(text -> !text.isBlank())
                .reduce((left, right) -> left + "\n" + right)
                .orElse("");
        boolean piiDetected = containsPii(originalText);
        String anonymizedText = anonymize(originalText);
        String normalized = anonymizedText.toLowerCase(Locale.GERMAN);
        List<AnalysisFindingResult> findings = new ArrayList<>();

        TOPICS.forEach((keyword, topic) -> {
            if (normalized.contains(keyword)
                    && findings.stream().noneMatch(finding ->
                    finding.category().equals(topic.category()))) {
                findings.add(new AnalysisFindingResult(
                        topic.category(),
                        topic.label(),
                        topic.sentiment(),
                        topic.priority(),
                        topic.score(),
                        anonymizedText));
            }
        });

        double averageRating = input.answers().stream()
                .map(answer -> answer.numericValue())
                .filter(value -> value != null)
                .mapToDouble(BigDecimal::doubleValue)
                .average()
                .orElse(3.0);
        boolean negative = findings.stream()
                .anyMatch(finding -> finding.sentiment().equals("NEGATIVE"))
                || averageRating < 2.8;
        boolean positive = !negative && (findings.stream()
                .anyMatch(finding -> finding.sentiment().equals("POSITIVE"))
                || averageRating >= 4.0);
        String sentiment = negative ? "NEGATIVE" : positive ? "POSITIVE" : "NEUTRAL";
        String risk = findings.stream().anyMatch(finding -> finding.priority().equals("HIGH"))
                ? "HIGH"
                : negative ? "MEDIUM" : "LOW";

        if (findings.isEmpty()) {
            findings.add(new AnalysisFindingResult(
                    "GENERAL",
                    "Allgemeines Mitarbeiterfeedback",
                    sentiment,
                    risk.equals("HIGH") ? "HIGH" : "LOW",
                    Math.min(1.0, Math.max(0.0, averageRating / 5.0)),
                    anonymizedText.isBlank() ? null : anonymizedText));
        }

        return new AnalysisResult(
                sentiment,
                buildSummary(findings, sentiment),
                piiDetected,
                risk,
                "demo-analysis-v1",
                List.copyOf(findings));
    }

    private boolean containsPii(String text) {
        return EMAIL.matcher(text).find()
                || PHONE.matcher(text).find()
                || PERSON.matcher(text).find();
    }

    private String anonymize(String text) {
        String anonymized = EMAIL.matcher(text).replaceAll("[E-MAIL ENTFERNT]");
        anonymized = PHONE.matcher(anonymized).replaceAll("[TELEFONNUMMER ENTFERNT]");
        return PERSON.matcher(anonymized).replaceAll("[PERSON ENTFERNT]");
    }

    private String buildSummary(
            List<AnalysisFindingResult> findings,
            String sentiment) {
        String topics = findings.stream()
                .limit(3)
                .map(AnalysisFindingResult::label)
                .reduce((left, right) -> left + ", " + right)
                .orElse("allgemeines Feedback");
        return "Die Rückmeldung ist überwiegend "
                + switch (sentiment) {
                    case "POSITIVE" -> "positiv";
                    case "NEGATIVE" -> "negativ";
                    default -> "neutral";
                }
                + ". Erkannte Themen: "
                + topics
                + ".";
    }

    private static Map<String, Topic> topics() {
        Map<String, Topic> topics = new LinkedHashMap<>();
        topics.put("überlast", new Topic(
                "WORKLOAD", "Hohe Arbeitsbelastung", "NEGATIVE", "HIGH", 0.95));
        topics.put("arbeitslast", new Topic(
                "WORKLOAD", "Hohe Arbeitsbelastung", "NEGATIVE", "HIGH", 0.90));
        topics.put("stress", new Topic(
                "WORKLOAD", "Belastung und Stress", "NEGATIVE", "HIGH", 0.88));
        topics.put("führung", new Topic(
                "LEADERSHIP", "Verbesserungsbedarf bei Führung", "NEGATIVE", "HIGH", 0.86));
        topics.put("chef", new Topic(
                "LEADERSHIP", "Verbesserungsbedarf bei Führung", "NEGATIVE", "HIGH", 0.82));
        topics.put("kommunikation", new Topic(
                "COMMUNICATION", "Interne Kommunikation", "NEGATIVE", "MEDIUM", 0.78));
        topics.put("wertschätzung", new Topic(
                "RECOGNITION", "Wahrgenommene Wertschätzung", "NEGATIVE", "MEDIUM", 0.76));
        topics.put("teamzusammenhalt", new Topic(
                "TEAMWORK", "Guter Teamzusammenhalt", "POSITIVE", "LOW", 0.84));
        topics.put("gutes team", new Topic(
                "TEAMWORK", "Guter Teamzusammenhalt", "POSITIVE", "LOW", 0.80));
        return Map.copyOf(topics);
    }

    private record Topic(
            String category,
            String label,
            String sentiment,
            String priority,
            double score) {
    }
}
