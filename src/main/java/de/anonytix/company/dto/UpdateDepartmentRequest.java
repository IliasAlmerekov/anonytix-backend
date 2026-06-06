package de.anonytix.company.dto;

import jakarta.validation.constraints.Size;

public record UpdateDepartmentRequest(
        @Size(min = 1, max = 160) String name,
        @Size(min = 1, max = 50) String code,
        Boolean active) {
}
