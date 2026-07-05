package com.edusuite.platform.academics;

import com.edusuite.platform.tenant.TenantScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

/**
 * First real tenant-scoped entity in the system - deliberately simple, used both as the
 * template new entities should copy and as the subject of TenantIsolationIT.
 */
@Entity
@Table(name = "academic_year")
public class AcademicYear extends TenantScopedEntity {

    @Column(nullable = false)
    private String label; // e.g. "2026-27"

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private boolean current;

    protected AcademicYear() {
        // JPA
    }

    public AcademicYear(String label, LocalDate startDate, LocalDate endDate, boolean current) {
        this.label = label;
        this.startDate = startDate;
        this.endDate = endDate;
        this.current = current;
    }

    public String getLabel() {
        return label;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public boolean isCurrent() {
        return current;
    }
}
