package de.anonytix.dashboard.dto;

import de.anonytix.dashboard.dto.DashboardTypes.DepartmentCategoryScore;
import de.anonytix.dashboard.dto.DashboardTypes.DepartmentReference;
import de.anonytix.dashboard.dto.DashboardTypes.Topic;
import de.anonytix.dashboard.dto.DashboardTypes.TrendPoint;
import java.util.List;
import java.util.UUID;

public record DepartmentDashboardResponse(
        DepartmentReference department,
        UUID campaignId,
        int sampleSize,
        int minimumGroupSize,
        boolean visible,
        String suppressionReason,
        Double overallSatisfaction,
        Double companyAverage,
        List<DepartmentCategoryScore> categoryScores,
        List<TrendPoint> trend,
        List<Topic> topTopics) {
}
