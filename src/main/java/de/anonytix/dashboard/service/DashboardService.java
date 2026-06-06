package de.anonytix.dashboard.service;

import de.anonytix.dashboard.dto.DashboardOverviewResponse;
import de.anonytix.dashboard.dto.DashboardTypes.AiSummary;
import de.anonytix.dashboard.dto.DashboardTypes.CampaignReference;
import de.anonytix.dashboard.dto.DashboardTypes.CategoryScore;
import de.anonytix.dashboard.dto.DashboardTypes.CompanyReference;
import de.anonytix.dashboard.dto.DashboardTypes.DepartmentCategoryScore;
import de.anonytix.dashboard.dto.DashboardTypes.DepartmentHeatmapEntry;
import de.anonytix.dashboard.dto.DashboardTypes.DepartmentReference;
import de.anonytix.dashboard.dto.DashboardTypes.Kpi;
import de.anonytix.dashboard.dto.DashboardTypes.SentimentDistribution;
import de.anonytix.dashboard.dto.DashboardTypes.Topic;
import de.anonytix.dashboard.dto.DashboardTypes.TrendPoint;
import de.anonytix.dashboard.dto.DepartmentDashboardResponse;
import de.anonytix.dashboard.repository.DashboardQueryRepository;
import de.anonytix.dashboard.repository.DashboardQueryRepository.CategoryAggregate;
import de.anonytix.dashboard.repository.DashboardQueryRepository.DashboardContext;
import de.anonytix.dashboard.repository.DashboardQueryRepository.DepartmentAggregate;
import de.anonytix.dashboard.repository.DashboardQueryRepository.DepartmentInfo;
import de.anonytix.dashboard.repository.DashboardQueryRepository.TopicAggregate;
import de.anonytix.shared.error.ResourceNotFoundException;
import java.time.Instant;
import java.time.Month;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private static final String SUPPRESSION_REASON =
            "MINIMUM_GROUP_SIZE_NOT_REACHED";
    private static final String DISCLAIMER =
            "KI-generierte Zusammenfassung auf Basis anonymisierter "
                    + "und aggregierter Daten.";

    private final DashboardQueryRepository repository;

    public DashboardService(DashboardQueryRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public DashboardOverviewResponse overview(UUID companyId, UUID campaignId) {
        DashboardContext context = context(companyId, campaignId);
        int sampleSize = repository.sampleSize(companyId, campaignId, null);
        List<CategoryScore> categoryScores = categoryScores(companyId, campaignId);
        List<SentimentDistribution> sentiments =
                sentiments(companyId, campaignId, sampleSize);
        List<Topic> topics = topics(companyId, campaignId, null);
        Double satisfaction =
                repository.overallSatisfaction(companyId, campaignId, null);
        List<Kpi> kpis = kpis(
                sampleSize,
                satisfaction,
                sentiments,
                categoryScores,
                topics,
                repository.actionItems(companyId, campaignId).stream()
                        .filter(action -> !action.status().equals("DONE"))
                        .count());
        List<DepartmentHeatmapEntry> heatmap =
                departmentHeatmap(context, companyId, campaignId);
        Instant generatedAt = repository.latestAnalysisAt(companyId, campaignId);

        return new DashboardOverviewResponse(
                new CompanyReference(context.companyId(), context.companyName()),
                new CampaignReference(
                        context.campaignId(),
                        context.campaignName(),
                        context.startsAt(),
                        context.endsAt()),
                sampleSize,
                context.minimumGroupSize(),
                kpis,
                sentiments,
                categoryScores,
                trend(companyId, campaignId, null),
                heatmap,
                topics,
                new AiSummary(
                        aggregateSummary(topics, sampleSize),
                        generatedAt == null ? Instant.now() : generatedAt,
                        "aggregate-v1",
                        DISCLAIMER),
                repository.actionItems(companyId, campaignId));
    }

    @Transactional(readOnly = true)
    public DepartmentDashboardResponse department(
            UUID companyId,
            UUID departmentId,
            UUID campaignId) {
        DashboardContext context = context(companyId, campaignId);
        DepartmentInfo department = repository.findDepartment(companyId, departmentId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Abteilung " + departmentId + " wurde nicht gefunden."));
        int sampleSize =
                repository.sampleSize(companyId, campaignId, departmentId);
        if (sampleSize < context.minimumGroupSize()) {
            return new DepartmentDashboardResponse(
                    new DepartmentReference(department.id(), department.name()),
                    campaignId,
                    sampleSize,
                    context.minimumGroupSize(),
                    false,
                    SUPPRESSION_REASON,
                    null,
                    null,
                    List.of(),
                    List.of(),
                    List.of());
        }

        Map<String, Double> companyScores = repository
                .categoryScores(companyId, campaignId, null)
                .stream()
                .collect(Collectors.toMap(
                        CategoryAggregate::category,
                        aggregate -> round(aggregate.score())));
        List<DepartmentCategoryScore> scores = repository
                .categoryScores(companyId, campaignId, departmentId)
                .stream()
                .map(aggregate -> new DepartmentCategoryScore(
                        aggregate.category(),
                        categoryLabel(aggregate.category()),
                        round(aggregate.score()),
                        companyScores.get(aggregate.category())))
                .toList();
        return new DepartmentDashboardResponse(
                new DepartmentReference(department.id(), department.name()),
                campaignId,
                sampleSize,
                context.minimumGroupSize(),
                true,
                null,
                nullableRound(repository.overallSatisfaction(
                        companyId, campaignId, departmentId)),
                nullableRound(repository.overallSatisfaction(
                        companyId, campaignId, null)),
                scores,
                trend(companyId, campaignId, departmentId),
                topics(companyId, campaignId, departmentId));
    }

    private DashboardContext context(UUID companyId, UUID campaignId) {
        return repository.findContext(companyId, campaignId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Dashboard-Kontext für Firma "
                                + companyId
                                + " und Kampagne "
                                + campaignId
                                + " wurde nicht gefunden."));
    }

    private List<CategoryScore> categoryScores(UUID companyId, UUID campaignId) {
        return repository.categoryScores(companyId, campaignId, null).stream()
                .map(aggregate -> new CategoryScore(
                        aggregate.category(),
                        categoryLabel(aggregate.category()),
                        round(aggregate.score()),
                        null,
                        aggregate.sampleSize()))
                .toList();
    }

    private List<SentimentDistribution> sentiments(
            UUID companyId,
            UUID campaignId,
            int sampleSize) {
        Map<String, Integer> counts = repository.sentiments(companyId, campaignId).stream()
                .collect(Collectors.toMap(
                        aggregate -> aggregate.sentiment(),
                        aggregate -> aggregate.count()));
        return List.of("POSITIVE", "NEUTRAL", "NEGATIVE").stream()
                .map(sentiment -> {
                    int count = counts.getOrDefault(sentiment, 0);
                    double percentage =
                            sampleSize == 0 ? 0 : round(count * 100.0 / sampleSize);
                    return new SentimentDistribution(sentiment, percentage, count);
                })
                .toList();
    }

    private List<DepartmentHeatmapEntry> departmentHeatmap(
            DashboardContext context,
            UUID companyId,
            UUID campaignId) {
        List<DepartmentHeatmapEntry> entries = new ArrayList<>();
        for (DepartmentAggregate department :
                repository.departments(companyId, campaignId)) {
            boolean suppressed =
                    department.sampleSize() < context.minimumGroupSize();
            Map<String, Double> scores = null;
            if (!suppressed) {
                scores = repository
                        .categoryScores(companyId, campaignId, department.id())
                        .stream()
                        .collect(Collectors.toMap(
                                CategoryAggregate::category,
                                aggregate -> round(aggregate.score()),
                                (left, right) -> left,
                                LinkedHashMap::new));
            }
            entries.add(new DepartmentHeatmapEntry(
                    department.id(),
                    department.name(),
                    department.sampleSize(),
                    suppressed,
                    suppressed ? SUPPRESSION_REASON : null,
                    scores));
        }
        return List.copyOf(entries);
    }

    private List<Topic> topics(
            UUID companyId,
            UUID campaignId,
            UUID departmentId) {
        return repository.topTopics(companyId, campaignId, departmentId).stream()
                .map(this::toTopic)
                .toList();
    }

    private Topic toTopic(TopicAggregate aggregate) {
        return new Topic(
                null,
                aggregate.category(),
                aggregate.label(),
                aggregate.mentions(),
                aggregate.sentiment(),
                aggregate.priority(),
                null);
    }

    private List<TrendPoint> trend(
            UUID companyId,
            UUID campaignId,
            UUID departmentId) {
        return repository.trend(companyId, campaignId, departmentId).stream()
                .map(point -> new TrendPoint(
                        point.period(),
                        monthLabel(point.period()),
                        round(point.score()),
                        point.sampleSize()))
                .toList();
    }

    private List<Kpi> kpis(
            int sampleSize,
            Double satisfaction,
            List<SentimentDistribution> sentiments,
            List<CategoryScore> categories,
            List<Topic> topics,
            long openActions) {
        List<Kpi> kpis = new ArrayList<>();
        kpis.add(new Kpi(
                "OVERALL_SATISFACTION",
                "Gesamtzufriedenheit",
                satisfaction == null ? 0 : round(satisfaction),
                "OUT_OF_5",
                null,
                null,
                null));
        kpis.add(new Kpi(
                "APPROVED_RESPONSES",
                "Freigegebene Antworten",
                sampleSize,
                "COUNT",
                null,
                null,
                null));
        double positive = sentiments.stream()
                .filter(value -> value.sentiment().equals("POSITIVE"))
                .mapToDouble(SentimentDistribution::percentage)
                .findFirst()
                .orElse(0);
        kpis.add(new Kpi(
                "POSITIVE_SENTIMENT",
                "Positive Stimmung",
                positive,
                "PERCENT",
                null,
                null,
                null));
        long criticalTopics = topics.stream()
                .filter(topic -> topic.priority().equals("HIGH"))
                .count();
        kpis.add(new Kpi(
                "CRITICAL_TOPICS",
                "Kritische Themen",
                criticalTopics,
                "COUNT",
                null,
                null,
                null));
        categories.stream()
                .max(java.util.Comparator.comparingDouble(CategoryScore::score))
                .ifPresent(category -> kpis.add(new Kpi(
                        "STRONGEST_CATEGORY",
                        "Stärkster Bereich",
                        category.score(),
                        "OUT_OF_5",
                        category.label(),
                        null,
                        null)));
        categories.stream()
                .min(java.util.Comparator.comparingDouble(CategoryScore::score))
                .ifPresent(category -> kpis.add(new Kpi(
                        "WEAKEST_CATEGORY",
                        "Schwächster Bereich",
                        category.score(),
                        "OUT_OF_5",
                        category.label(),
                        null,
                        null)));
        kpis.add(new Kpi(
                "OPEN_ACTIONS",
                "Offene Maßnahmen",
                openActions,
                "COUNT",
                null,
                null,
                null));
        return List.copyOf(kpis);
    }

    private String aggregateSummary(List<Topic> topics, int sampleSize) {
        if (sampleSize == 0) {
            return "Für diese Kampagne liegen noch keine freigegebenen "
                    + "Rückmeldungen vor.";
        }
        if (topics.isEmpty()) {
            return "Es wurden "
                    + sampleSize
                    + " freigegebene Rückmeldungen ausgewertet. "
                    + "Noch hat sich kein häufiges Thema herausgebildet.";
        }
        String labels = topics.stream()
                .limit(3)
                .map(Topic::label)
                .collect(Collectors.joining(", "));
        return "Es wurden "
                + sampleSize
                + " freigegebene Rückmeldungen ausgewertet. "
                + "Die häufigsten Themen sind: "
                + labels
                + ".";
    }

    private String categoryLabel(String category) {
        return switch (category) {
            case "TEAMWORK" -> "Teamzusammenhalt";
            case "WORK_ENVIRONMENT" -> "Arbeitsumgebung";
            case "COMMUNICATION" -> "Kommunikation";
            case "LEADERSHIP" -> "Führung";
            case "WORKLOAD" -> "Arbeitsbelastung";
            case "RECOGNITION" -> "Wertschätzung";
            case "PROCESSES" -> "Prozesse";
            default -> category;
        };
    }

    private String monthLabel(String period) {
        int month = Integer.parseInt(period.substring(5, 7));
        return Month.of(month).getDisplayName(TextStyle.FULL, Locale.GERMAN);
    }

    private Double nullableRound(Double value) {
        return value == null ? null : round(value);
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }
}
