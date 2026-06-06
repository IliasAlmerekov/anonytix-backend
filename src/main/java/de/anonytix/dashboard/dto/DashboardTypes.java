package de.anonytix.dashboard.dto;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public final class DashboardTypes {

    private DashboardTypes() {
    }

    public record CompanyReference(UUID id, String name) {
    }

    public record CampaignReference(
            UUID id,
            String name,
            Instant startsAt,
            Instant endsAt) {
    }

    public record DepartmentReference(UUID id, String name) {
    }

    public record Kpi(
            String key,
            String label,
            double value,
            String unit,
            String detail,
            Double change,
            String changeLabel) {
    }

    public record SentimentDistribution(
            String sentiment,
            double percentage,
            int count) {
    }

    public record CategoryScore(
            String category,
            String label,
            double score,
            Double previousScore,
            int sampleSize) {
    }

    public record DepartmentCategoryScore(
            String category,
            String label,
            double score,
            Double companyScore) {
    }

    public record TrendPoint(
            String period,
            String label,
            double score,
            Integer sampleSize) {
    }

    public record DepartmentHeatmapEntry(
            UUID departmentId,
            String departmentName,
            int sampleSize,
            boolean suppressed,
            String suppressionReason,
            Map<String, Double> scores) {
    }

    public record Topic(
            UUID id,
            String category,
            String label,
            int mentions,
            String sentiment,
            String priority,
            Double trendPercentage) {
    }

    public record AiSummary(
            String summary,
            Instant generatedAt,
            String model,
            String disclaimer) {
    }

    public record ActionItem(
            UUID id,
            String title,
            String description,
            String category,
            String priority,
            String status,
            String source) {
    }
}
