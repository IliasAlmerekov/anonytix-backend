package de.anonytix.company.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateDepartmentRequest(
        @NotBlank @Size(max = 160) String name,
        @NotBlank @Size(max = 50) String code) {
}
