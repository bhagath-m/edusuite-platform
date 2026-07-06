package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.TenantContext;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AuditLogServiceTest {

    private final AuditLogRepository auditLogRepository = mock(AuditLogRepository.class);
    private final AuditLogService auditLogService = new AuditLogService(auditLogRepository);

    @Test
    void findByCurrentTenantQueriesWithTenantContext() {
        UUID tenantId = UUID.randomUUID();
        AuditLog log = new AuditLog("actor", "UPDATE", "Entity", "2", null, null, null, null);
        when(auditLogRepository.findByTenantId(tenantId)).thenReturn(List.of(log));

        List<AuditLog> result = TenantContext.runAs(tenantId, auditLogService::findByCurrentTenant);

        assertThat(result).containsExactly(log);
        verify(auditLogRepository).findByTenantId(tenantId);
    }
}
