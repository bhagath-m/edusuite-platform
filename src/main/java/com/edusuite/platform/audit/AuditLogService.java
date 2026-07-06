package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantContext;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Read-only query service for {@link AuditLog} records.
 */
@Service
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * @return all audit logs for the current tenant
     */
    public List<AuditLog> findByCurrentTenant() {
        return auditLogRepository.findByTenantId(TenantContext.get());
    }
}
