package com.edusuite.platform.tenant;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
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
class TenantRepositoryTest {

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
    private TenantRepository tenantRepository;

    @Test
    void saveAndFindById() {
        Tenant tenant = new Tenant("Alpha School", "alpha", "SCHOOL");
        Tenant saved = tenantRepository.save(tenant);
        UUID id = saved.getId();

        var found = tenantRepository.findById(id);

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isEqualTo(id);
        assertThat(found.get().getName()).isEqualTo("Alpha School");
        assertThat(found.get().getSubdomain()).isEqualTo("alpha");
        assertThat(found.get().getInstituteType()).isEqualTo("SCHOOL");
    }

    @Test
    void findBySubdomainReturnsMatchingTenant() {
        Tenant tenant = new Tenant("Beta College", "beta", "COLLEGE");
        tenantRepository.save(tenant);

        var found = tenantRepository.findBySubdomain("beta");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Beta College");
        assertThat(found.get().getSubdomain()).isEqualTo("beta");
    }

    @Test
    void findBySubdomainReturnsEmptyWhenNotFound() {
        var found = tenantRepository.findBySubdomain("nonexistent");

        assertThat(found).isEmpty();
    }
}
