package com.edusuite.platform.academics;

import com.edusuite.platform.tenant.TenantContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class AcademicYearRepositoryTest {

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

    @Test
    void saveAndFindAllUnderTenantContext() {
        UUID tenantId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        AcademicYear year = new AcademicYear("2026-27", LocalDate.of(2026, 4, 1), LocalDate.of(2027, 3, 31), true);

        List<AcademicYear> saved = TenantContext.runAs(tenantId, () -> {
            academicYearRepository.save(year);
            return academicYearRepository.findAll();
        });

        assertThat(saved).hasSize(1);
        assertThat(saved.get(0).getLabel()).isEqualTo("2026-27");
        assertThat(saved.get(0).getTenantId()).isEqualTo(tenantId);
    }

    @Test
    void findByIdReturnsSavedEntity() {
        UUID tenantId = UUID.fromString("88888888-8888-8888-8888-888888888888");
        AcademicYear year = new AcademicYear("2027-28", LocalDate.of(2027, 4, 1), LocalDate.of(2028, 3, 31), false);

        AcademicYear saved = TenantContext.runAs(tenantId, () -> academicYearRepository.save(year));

        var found = TenantContext.runAs(tenantId, () -> academicYearRepository.findById(saved.getId()));

        assertThat(found).isPresent();
        assertThat(found.get().getLabel()).isEqualTo("2027-28");
    }
}
