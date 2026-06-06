package de.anonytix.company.domain;

import de.anonytix.shared.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, unique = true, length = 120)
    private String slug;

    @Column(name = "minimum_group_size", nullable = false)
    private int minimumGroupSize = 5;

    protected Company() {
    }

    public Company(String name, String slug) {
        this.name = name;
        this.slug = slug;
    }

    public String getName() {
        return name;
    }

    public String getSlug() {
        return slug;
    }

    public int getMinimumGroupSize() {
        return minimumGroupSize;
    }
}
