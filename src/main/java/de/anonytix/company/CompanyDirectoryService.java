package de.anonytix.company;

import de.anonytix.company.repository.CompanyRepository;
import de.anonytix.company.repository.DepartmentRepository;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
class CompanyDirectoryService implements CompanyDirectory {

    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;

    CompanyDirectoryService(
            CompanyRepository companyRepository,
            DepartmentRepository departmentRepository) {
        this.companyRepository = companyRepository;
        this.departmentRepository = departmentRepository;
    }

    @Override
    public boolean companyExists(UUID companyId) {
        return companyRepository.existsById(companyId);
    }

    @Override
    public boolean departmentsBelongToCompany(
            UUID companyId,
            Collection<UUID> departmentIds) {
        return departmentIds.stream()
                .allMatch(id -> departmentRepository.findByIdAndCompanyId(id, companyId).isPresent());
    }

    @Override
    public List<DepartmentReference> activeDepartments(UUID companyId) {
        return departmentRepository.findAllByCompanyIdOrderByNameAsc(companyId).stream()
                .filter(department -> department.isActive())
                .map(department -> new DepartmentReference(
                        department.getId(),
                        department.getName()))
                .toList();
    }
}
