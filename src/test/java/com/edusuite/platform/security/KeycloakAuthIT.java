package com.edusuite.platform.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test verifying that the Spring Boot resource server accepts
 * Keycloak-issued JWTs for an authenticated endpoint.
 *
 * A Keycloak 25.0 container is started, the realm is imported from
 * {@code keycloak/realm-export.json}, and a password grant is used to obtain
 * an access token for a realm user. The token is then presented to a
 * test-only REST controller that requires authentication.
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class KeycloakAuthIT {

    private static final String REALM_NAME = "edusuite";
    private static final String CLIENT_ID = "edusuite-app";
    private static final String TEST_USERNAME = "admin@tenant-a.test";
    private static final String TEST_PASSWORD = "password123";

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("edusuite_test")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> keycloak = new GenericContainer<>("quay.io/keycloak/keycloak:25.0")
            .withExposedPorts(8080)
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withCopyToContainer(
                    MountableFile.forHostPath("keycloak/realm-export.json"),
                    "/opt/keycloak/data/import/realm-export.json")
            .withCommand("start-dev", "--import-realm")
            .waitingFor(Wait.forHttp("/realms/" + REALM_NAME).forStatusCode(200));

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri",
                () -> "http://localhost:" + keycloak.getFirstMappedPort() + "/realms/" + REALM_NAME);
    }

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void authenticatedEndpointReturnsPrincipalNameWithValidKeycloakJwt() {
        String token = obtainAccessToken();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(
                "/test/keycloak-auth", HttpMethod.GET, request, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(TEST_USERNAME);
    }

    private String obtainAccessToken() {
        String tokenUrl = "http://localhost:" + keycloak.getFirstMappedPort()
                + "/realms/" + REALM_NAME + "/protocol/openid-connect/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("grant_type", "password");
        body.add("client_id", CLIENT_ID);
        body.add("username", TEST_USERNAME);
        body.add("password", TEST_PASSWORD);
        body.add("scope", "tenant");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        RestTemplate client = new RestTemplate();
        ResponseEntity<Map> response = client.postForEntity(tokenUrl, request, Map.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsKey("access_token");

        return (String) response.getBody().get("access_token");
    }

    @TestConfiguration
    static class TestConfig {

        @RestController
        static class KeycloakAuthTestController {

            @GetMapping("/test/keycloak-auth")
            @PreAuthorize("isAuthenticated()")
            public ResponseEntity<String> me(@AuthenticationPrincipal Jwt jwt) {
                return ResponseEntity.ok(jwt.getClaimAsString("preferred_username"));
            }
        }
    }
}
