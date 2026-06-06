package de.anonytix.company.web;

import de.anonytix.company.dto.CreateDepartmentRequest;
import de.anonytix.company.dto.DepartmentResponse;
import de.anonytix.company.dto.UpdateDepartmentRequest;
import de.anonytix.company.service.DepartmentService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/companies/{companyId}/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    List<DepartmentResponse> list(@PathVariable UUID companyId) {
        return departmentService.list(companyId);
    }

    @PostMapping
    ResponseEntity<DepartmentResponse> create(
            @PathVariable UUID companyId,
            @Valid @RequestBody CreateDepartmentRequest request) {
        DepartmentResponse created = departmentService.create(companyId, request);
        return ResponseEntity.created(URI.create(
                        "/api/v1/companies/%s/departments/%s".formatted(companyId, created.id())))
                .body(created);
    }

    @PatchMapping("/{departmentId}")
    DepartmentResponse update(
            @PathVariable UUID companyId,
            @PathVariable UUID departmentId,
            @Valid @RequestBody UpdateDepartmentRequest request) {
        return departmentService.update(companyId, departmentId, request);
    }
}
