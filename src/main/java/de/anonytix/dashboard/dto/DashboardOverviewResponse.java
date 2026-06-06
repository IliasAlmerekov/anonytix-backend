package de.anonytix.dashboard.dto;

import de.anonytix.dashboard.dto.DashboardTypes.ActionItem;
import de.anonytix.dashboard.dto.DashboardTypes.AiHighlight;
import de.anonytix.dashboard.dto.DashboardTypes.AiSummary;
import de.anonytix.dashboard.dto.DashboardTypes.CampaignReference;
import de.anonytix.dashboard.dto.DashboardTypes.CategoryScore;
import de.anonytix.dashboard.dto.DashboardTypes.CompanyReference;
import de.anonytix.dashboard.dto.DashboardTypes.DepartmentHeatmapEntry;
import de.anonytix.dashboard.dto.DashboardTypes.Kpi;
import de.anonytix.dashboard.dto.DashboardTypes.MonthlyFeedback;
import de.anonytix.dashboard.dto.DashboardTypes.SentimentDistribution;
import de.anonytix.dashboard.dto.DashboardTypes.Topic;
import de.anonytix.dashboard.dto.DashboardTypes.TrendPoint;
import java.util.List;
import java.util.Map;

public record DashboardOverviewResponse(
        CompanyReference company,
        CampaignReference campaign,
        Integer selectedYear,
        List<Integer> years,
        int sampleSize,
        int minimumGroupSize,
        List<Kpi> kpis,
        List<SentimentDistribution> sentimentDistribution,
        List<CategoryScore> categoryScores,
        List<TrendPoint> satisfactionTrend,
        List<MonthlyFeedback> feedbackByMonth,
        List<Map<String, Object>> satisfactionByYear,
        List<DepartmentHeatmapEntry> departmentHeatmap,
        List<Topic> topTopics,
        AiSummary aiSummary,
        List<AiHighlight> aiHighlights,
        List<ActionItem> recommendedActions) {
}
