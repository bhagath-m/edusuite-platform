package com.edusuite.platform.audit;

import com.edusuite.platform.tenant.Tenant;
import com.edusuite.platform.tenant.TenantContext;
import com.edusuite.platform.tenant.TenantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@Import({AuditConfig.class, AuditAspect.class, DummyAuditedService.class, JacksonAutoConfiguration.class})
class AuditAspectTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("edusuite_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private DummyAuditedService service;

    @Autowired
    private DummyAuditedEntityRepository dummyRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private TenantRepository tenantRepository;

    private UUID tenantId;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        dummyRepository.deleteAll();
        Tenant tenant = new Tenant("Audit Test School", "audit-test", "SCHOOL");
        tenantId = tenantRepository.save(tenant).getId();
    }

    @Test
    void creatingAuditedEntityWritesAuditRow() {
        DummyAuditedEntity created = service.create(tenantId, "Alice");

        var logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);

        AuditLog log = logs.get(0);
        assertThat(log.getAction()).isEqualTo("CREATE_DUMMY");
        assertThat(log.getEntityType()).isEqualTo(DummyAuditedEntity.class.getSimpleName());
        assertThat(log.getEntityId()).isEqualTo(created.getId().toString());
        assertThat(log.getActor()).isEqualTo("anonymous");
        assertThat(log.getAfterJson()).contains("Alice");
        assertThat(log.getClientIp()).isNull();
        assertThat(log.getUserAgent()).isNull();
    }

    @Test
    void updatingAuditedEntityWritesAuditRow() {
        DummyAuditedEntity entity = TenantContext.runAs(tenantId, () -> {
            DummyAuditedEntity e = new DummyAuditedEntity("Alice");
            return dummyRepository.save(e);
        });
        auditLogRepository.deleteAll();

        DummyAuditedEntity updated = service.update(tenantId, entity.getId(), "Bob");

        var logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);

        AuditLog log = logs.get(0);
        assertThat(log.getAction()).isEqualTo("UPDATE_DUMMY");
        assertThat(log.getEntityType()).isEqualTo(DummyAuditedEntity.class.getSimpleName());
        assertThat(log.getEntityId()).isEqualTo(updated.getId().toString());
        assertThat(log.getActor()).isEqualTo("anonymous");
        assertThat(log.getAfterJson()).contains("Bob");
        assertThat(log.getClientIp()).isNull();
        assertThat(log.getUserAgent()).isNull();
    }

    @Test
    void deletingAuditedEntityWritesAuditRow() {
        DummyAuditedEntity entity = TenantContext.runAs(tenantId, () -> {
            DummyAuditedEntity e = new DummyAuditedEntity("Alice");
            return dummyRepository.save(e);
        });
        auditLogRepository.deleteAll();

        DummyAuditedEntity deleted = service.delete(tenantId, entity.getId());

        var logs = auditLogRepository.findAll();
        assertThat(logs).hasSize(1);

        AuditLog log = logs.get(0);
        assertThat(log.getAction()).isEqualTo("DELETE_DUMMY");
        assertThat(log.getEntityType()).isEqualTo(DummyAuditedEntity.class.getSimpleName());
        assertThat(log.getEntityId()).isEqualTo(entity.getId().toString());
        assertThat(log.getActor()).isEqualTo("anonymous");
        assertThat(log.getAfterJson()).contains(deleted.getName());
        assertThat(log.getClientIp()).isNull();
        assertThat(log.getUserAgent()).isNull();
    }
}
