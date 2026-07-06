package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Test-only service used to verify {@link AuditAspect} behavior.
 */
@Service
public class DummyAuditedService {

    private final DummyAuditedEntityRepository repository;

    public DummyAuditedService(DummyAuditedEntityRepository repository) {
        this.repository = repository;
    }

    @Audited(action = "CREATE_DUMMY")
    public DummyAuditedEntity create(UUID tenantId, String name) {
        return TenantContext.runAs(tenantId, () -> {
            DummyAuditedEntity entity = new DummyAuditedEntity(name);
            return repository.save(entity);
        });
    }

    @Audited(action = "UPDATE_DUMMY")
    public DummyAuditedEntity update(UUID tenantId, UUID id, String newName) {
        return TenantContext.runAs(tenantId, () -> {
            DummyAuditedEntity entity = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
            entity.setName(newName);
            return repository.save(entity);
        });
    }

    @Audited(action = "DELETE_DUMMY")
    public DummyAuditedEntity delete(UUID tenantId, UUID id) {
        return TenantContext.runAs(tenantId, () -> {
            DummyAuditedEntity entity = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Entity not found: " + id));
            repository.delete(entity);
            return entity;
        });
    }
}
