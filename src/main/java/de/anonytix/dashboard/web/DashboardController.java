package de.anonytix.dashboard.web;

import de.anonytix.dashboard.dto.DashboardOverviewResponse;
import de.anonytix.dashboard.dto.DepartmentDashboardResponse;
import de.anonytix.dashboard.service.DashboardService;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/dashboard")
public class DashboardController {

    private final DashboardService service;

    public DashboardController(DashboardService service) {
        this.service = service;
    }

    @GetMapping("/overview")
    public DashboardOverviewResponse overview(
            @PathVariable UUID companyId,
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(required = false) Integer year) {
        return service.overview(companyId, campaignId, year);
    }

    @GetMapping("/departments/{departmentId}")
    public DepartmentDashboardResponse department(
            @PathVariable UUID companyId,
            @PathVariable UUID departmentId,
            @RequestParam(required = false) UUID campaignId,
            @RequestParam(required = false) Integer year) {
        return service.department(companyId, departmentId, campaignId, year);
    }
}
