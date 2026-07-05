package com.edusuite.platform.tenant;

import com.edusuite.platform.academics.AcademicYear;
import com.edusuite.platform.academics.AcademicYearRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * RELEASE-BLOCKING TEST SUITE. If this fails, do not merge, do not deploy, regardless of what
 * else is failing or how urgent the release is. A failure here means tenant data isolation is
 * broken at the database layer.
 *
 * This test intentionally does NOT mock TenantContext/Hibernate multi-tenancy - it runs against
 * a real Postgres (via Testcontainers) with real RLS policies, because the whole point is to
 * catch the class of bug where the Java code "looks right" but the SQL-level enforcement isn't
 * actually wired correctly.
 */
@Testcontainers
@SpringBootTest
class TenantIsolationIT {

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
    private AcademicYearRepository academicYearRepository;

    @Autowired
    private jakarta.persistence.EntityManager entityManager;

    private static UUID tenantAId;
    private static UUID tenantBId;

    @BeforeAll
    static void setUpTenants() {
        // Direct JDBC insert for the platform-level Tenant rows (unscoped table, no
        // TenantContext needed) - kept deliberately outside the repository layer to avoid
        // coupling this bootstrap step to whichever Tenant creation API exists later.
        tenantAId = UUID.randomUUID();
        tenantBId = UUID.randomUUID();
        try (var conn = postgres.createConnection("");
             var stmt = conn.prepareStatement(
                     "INSERT INTO tenant (id, name, subdomain, institute_type) VALUES (?, ?, ?, 'SCHOOL')")) {
            stmt.setObject(1, tenantAId);
            stmt.setString(2, "Tenant A School");
            stmt.setString(3, "tenant-a");
            stmt.executeUpdate();

            stmt.setObject(1, tenantBId);
            stmt.setString(2, "Tenant B School");
            stmt.setString(3, "tenant-b");
            stmt.executeUpdate();
        } catch (Exception e) {
            throw new RuntimeException("Failed to seed test tenants", e);
        }
    }

    @Test
    void tenantCannotSeeAnotherTenantsRows() {
        // Create one AcademicYear as Tenant A
        UUID createdId = TenantContext.runAs(tenantAId, () -> {
            AcademicYear year = new AcademicYear("2026-27 (Tenant A)", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), true);
            academicYearRepository.saveAndFlush(year);
            entityManager.clear(); // force a real DB round-trip on next read, not a persistence-context cache hit
            return year.getId();
        });

        // Attempt to read it back as Tenant B
        TenantContext.runAs(tenantBId, () -> {
            entityManager.clear();
            List<AcademicYear> visibleToB = academicYearRepository.findAll();
            assertThat(visibleToB)
                    .as("Tenant B must not see any AcademicYear rows created by Tenant A")
                    .isEmpty();

            var directLookup = academicYearRepository.findById(createdId);
            assertThat(directLookup)
                    .as("Direct findById of another tenant's row must also come back empty, not just filtered list queries")
                    .isEmpty();
            return null;
        });

        // Sanity check: Tenant A can still see its own row (proves the test isn't just broken/empty-by-accident)
        TenantContext.runAs(tenantAId, () -> {
            entityManager.clear();
            List<AcademicYear> visibleToA = academicYearRepository.findAll();
            assertThat(visibleToA).hasSize(1);
            return null;
        });
    }

    @Test
    void writingWithoutTenantContextFailsClosed() {
        TenantContext.clear();
        AcademicYear orphan = new AcademicYear("No Tenant", LocalDate.now(), LocalDate.now().plusYears(1), false);
        org.junit.jupiter.api.Assertions.assertThrows(
                IllegalStateException.class,
                () -> academicYearRepository.saveAndFlush(orphan),
                "Persisting a tenant-scoped entity with no TenantContext set must fail loudly, "
              + "never silently default to some tenant or null.");
    }
}
