package de.anonytix.analysis.gateway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.anonytix.feedback.FeedbackAnalysisInput;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(
        name = "anonytix.ai.provider",
        havingValue = "openai")
public class OpenAiFeedbackAnalysisGateway implements FeedbackAnalysisGateway {

    private static final String SYSTEM_PROMPT = """
            Du analysierst anonymes Mitarbeiterfeedback für ein HR-Dashboard.
            Entferne oder verallgemeinere alle Namen, E-Mail-Adressen,
            Telefonnummern, konkreten Rollen und andere identifizierende Hinweise.
            Gib keine Identität und keinen Originaltext zurück.

            Erlaubte Werte:
            overallSentiment und finding.sentiment: POSITIVE, NEUTRAL, NEGATIVE
            riskLevel und finding.priority: LOW, MEDIUM, HIGH

            Erzeuge eine kurze deutsche Zusammenfassung und strukturierte Findings.
            Kategorien sollen stabile englische Schlüssel wie WORKLOAD, LEADERSHIP,
            COMMUNICATION, TEAMWORK, RECOGNITION oder GENERAL sein.
            Der score muss zwischen 0 und 1 liegen.
            """;

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper;

    public OpenAiFeedbackAnalysisGateway(
            ChatClient.Builder chatClientBuilder,
            ObjectMapper objectMapper) {
        this.chatClient = chatClientBuilder.build();
        this.objectMapper = objectMapper;
    }

    @Override
    public AnalysisResult analyze(FeedbackAnalysisInput input) {
        AnalysisResult result = chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user("Analysiere diese Feedback-Daten:\n" + serialize(input))
                .call()
                .entity(AnalysisResult.class);
        if (result == null) {
            throw new IllegalStateException("OpenAI hat kein Analyseergebnis geliefert.");
        }
        return result;
    }

    private String serialize(FeedbackAnalysisInput input) {
        try {
            return objectMapper.writeValueAsString(input);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                    "Feedback konnte nicht für die KI serialisiert werden.",
                    exception);
        }
    }
}
