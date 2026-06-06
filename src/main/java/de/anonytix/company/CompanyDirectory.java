package de.anonytix.company;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface CompanyDirectory {

    boolean companyExists(UUID companyId);

    boolean departmentsBelongToCompany(UUID companyId, Collection<UUID> departmentIds);

    List<DepartmentReference> activeDepartments(UUID companyId);
}
