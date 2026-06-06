package de.anonytix.company.domain;

import de.anonytix.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private boolean active = true;

    protected Department() {
    }

    public Department(Company company, String name, String code) {
        this.company = company;
        this.name = name;
        this.code = code;
    }

    public Company getCompany() {
        return company;
    }

    public String getName() {
        return name;
    }

    public String getCode() {
        return code;
    }

    public boolean isActive() {
        return active;
    }

    public void update(String name, String code, Boolean active) {
        if (name != null) {
            this.name = name;
        }
        if (code != null) {
            this.code = code;
        }
        if (active != null) {
            this.active = active;
        }
    }
}
