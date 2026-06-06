package de.anonytix.company.dto;

import java.util.UUID;

public record DepartmentResponse(UUID id, String name, String code, boolean active) {
}
