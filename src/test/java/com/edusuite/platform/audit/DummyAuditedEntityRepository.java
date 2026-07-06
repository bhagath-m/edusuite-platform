package com.edusuite.platform.audit;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

/**
 * Test-only repository for {@link DummyAuditedEntity}.
 */
public interface DummyAuditedEntityRepository extends JpaRepository<DummyAuditedEntity, UUID> {
}
