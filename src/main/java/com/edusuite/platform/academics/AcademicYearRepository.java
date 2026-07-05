package com.edusuite.platform.academics;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, UUID> {
    // Deliberately NO findByTenantId-style methods here: tenant scoping is handled by RLS +
    // the current_tenant session setting, not by adding tenant_id to every query manually.
    // Standard findAll()/findById() etc. will only ever return the current tenant's rows.
}
