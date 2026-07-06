package com.edusuite.platform.audit;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Optional;

/**
 * Authenticated endpoint for reading the current tenant's audit log.
 */
@RestController
@RequestMapping("/api/v1/audit")
public class AuditLogController {

    private final AuditLogService auditLogService;
    private final AuditLogRepository auditLogRepository;

    public AuditLogController(AuditLogService auditLogService, AuditLogRepository auditLogRepository) {
        this.auditLogService = auditLogService;
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.findByCurrentTenant());
    }

    /**
     * Creates a minimal audit log entry for the current tenant and authenticated actor.
     * Actor and tenant are derived from the request context, not client input.
     */
    @PostMapping
    public ResponseEntity<AuditLog> createAuditLog(@Valid @RequestBody CreateAuditLogRequest request) {
        String actor = Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getName)
                .orElse("anonymous");
        AuditLog auditLog = new AuditLog(
                actor,
                request.action(),
                request.entityType(),
                request.entityId(),
                null,
                request.afterJson(),
                null,
                null
        );
        AuditLog saved = auditLogRepository.save(auditLog);
        return ResponseEntity.ok(saved);
    }

    public record CreateAuditLogRequest(
            @NotBlank String action,
            String entityType,
            String entityId,
            String afterJson
    ) {
    }
}
