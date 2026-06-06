package de.anonytix.company.service;

import de.anonytix.company.domain.Company;
import de.anonytix.company.domain.Department;
import de.anonytix.company.dto.CreateDepartmentRequest;
import de.anonytix.company.dto.DepartmentResponse;
import de.anonytix.company.dto.UpdateDepartmentRequest;
import de.anonytix.company.mapper.DepartmentMapper;
import de.anonytix.company.repository.CompanyRepository;
import de.anonytix.company.repository.DepartmentRepository;
import de.anonytix.shared.error.ConflictException;
import de.anonytix.shared.error.ResourceNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class DepartmentService {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final DepartmentMapper departmentMapper;

    public DepartmentService(
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository,
            DepartmentMapper departmentMapper) {
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
        this.departmentMapper = departmentMapper;
    }

    public List<DepartmentResponse> list(UUID companyId) {
        requireCompany(companyId);
        return departmentRepository.findAllByCompanyIdOrderByNameAsc(companyId).stream()
                .map(departmentMapper::toResponse)
                .toList();
    }

    @Transactional
    public DepartmentResponse create(UUID companyId, CreateDepartmentRequest request) {
        Company company = requireCompany(companyId);
        String code = normalizeCode(request.code());
        ensureCodeAvailable(companyId, code, null);
        Department department = departmentRepository.save(
                new Department(company, request.name().trim(), code));
        return departmentMapper.toResponse(department);
    }

    @Transactional
    public DepartmentResponse update(
            UUID companyId,
            UUID departmentId,
            UpdateDepartmentRequest request) {
        Department department = departmentRepository.findByIdAndCompanyId(departmentId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Abteilung wurde nicht gefunden."));
        String code = request.code() == null ? null : normalizeCode(request.code());
        if (code != null) {
            ensureCodeAvailable(companyId, code, departmentId);
        }
        String name = request.name() == null ? null : request.name().trim();
        department.update(name, code, request.active());
        return departmentMapper.toResponse(department);
    }

    private Company requireCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Firma wurde nicht gefunden."));
    }

    private void ensureCodeAvailable(UUID companyId, String code, UUID departmentId) {
        boolean exists = departmentId == null
                ? departmentRepository.existsByCompanyIdAndCodeIgnoreCase(companyId, code)
                : departmentRepository.existsByCompanyIdAndCodeIgnoreCaseAndIdNot(
                        companyId, code, departmentId);
        if (exists) {
            throw new ConflictException(
                    "DEPARTMENT_CODE_EXISTS",
                    "Der Abteilungscode wird in dieser Firma bereits verwendet.");
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }
}
