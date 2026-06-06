package de.anonytix.analysis;

import static org.assertj.core.api.Assertions.assertThat;

import de.anonytix.analysis.gateway.AnalysisResult;
import de.anonytix.analysis.gateway.DemoFeedbackAnalysisGateway;
import de.anonytix.feedback.FeedbackAnalysisInput;
import de.anonytix.feedback.FeedbackAnswerData;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DemoFeedbackAnalysisGatewayTest {

    @Test
    void removesCommonPersonalDataAndDetectsWorkloadRisk() {
        FeedbackAnalysisInput input = new FeedbackAnalysisInput(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(new FeedbackAnswerData(
                        UUID.randomUUID(),
                        null,
                        "Frau Beispiel und max@example.de: Ich bin total überlastet.",
                        null,
                        Set.of())));

        AnalysisResult result = new DemoFeedbackAnalysisGateway().analyze(input);

        assertThat(result.piiDetected()).isTrue();
        assertThat(result.riskLevel()).isEqualTo("HIGH");
        assertThat(result.findings()).anySatisfy(finding -> {
            assertThat(finding.category()).isEqualTo("WORKLOAD");
            assertThat(finding.anonymizedText())
                    .doesNotContain("Frau Beispiel")
                    .doesNotContain("max@example.de")
                    .contains("[PERSON ENTFERNT]")
                    .contains("[E-MAIL ENTFERNT]");
        });
    }
}
