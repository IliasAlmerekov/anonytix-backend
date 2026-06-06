package de.anonytix.dashboard.dto;

import de.anonytix.dashboard.dto.DashboardTypes.ActionItem;
import de.anonytix.dashboard.dto.DashboardTypes.AiSummary;
import de.anonytix.dashboard.dto.DashboardTypes.CampaignReference;
import de.anonytix.dashboard.dto.DashboardTypes.CategoryScore;
import de.anonytix.dashboard.dto.DashboardTypes.CompanyReference;
import de.anonytix.dashboard.dto.DashboardTypes.DepartmentHeatmapEntry;
import de.anonytix.dashboard.dto.DashboardTypes.Kpi;
import de.anonytix.dashboard.dto.DashboardTypes.SentimentDistribution;
import de.anonytix.dashboard.dto.DashboardTypes.Topic;
import de.anonytix.dashboard.dto.DashboardTypes.TrendPoint;
import java.util.List;

public record DashboardOverviewResponse(
        CompanyReference company,
        CampaignReference campaign,
        int sampleSize,
        int minimumGroupSize,
        List<Kpi> kpis,
        List<SentimentDistribution> sentimentDistribution,
        List<CategoryScore> categoryScores,
        List<TrendPoint> satisfactionTrend,
        List<DepartmentHeatmapEntry> departmentHeatmap,
        List<Topic> topTopics,
        AiSummary aiSummary,
        List<ActionItem> recommendedActions) {
}
