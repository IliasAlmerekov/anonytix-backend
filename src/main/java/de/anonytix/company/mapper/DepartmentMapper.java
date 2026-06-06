package de.anonytix.company.mapper;

import de.anonytix.company.domain.Department;
import de.anonytix.company.dto.DepartmentResponse;
import de.anonytix.shared.mapper.MappingConfig;
import org.mapstruct.Mapper;

@Mapper(config = MappingConfig.class)
public interface DepartmentMapper {

    DepartmentResponse toResponse(Department department);
}
