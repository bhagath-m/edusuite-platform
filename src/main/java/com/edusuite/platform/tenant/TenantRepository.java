package com.edusuite.platform.tenant;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Platform-level repository for {@link Tenant}.
 * <p>
 * This repository operates on the global tenant table, which is intentionally not scoped by
 * tenant_id. It should only be used by platform administration endpoints.
 */
public interface TenantRepository extends JpaRepository<Tenant, UUID> {

    /**
     * Find a tenant by its unique subdomain.
     *
     * @param subdomain the tenant subdomain
     * @return the matching tenant, or empty if none exists
     */
    Optional<Tenant> findBySubdomain(String subdomain);
}
