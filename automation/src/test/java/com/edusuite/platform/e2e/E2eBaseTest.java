package com.edusuite.platform.e2e;

import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.startupcheck.IsRunningStartupCheckStrategy;
import org.testcontainers.containers.output.OutputFrame;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.lifecycle.Startable;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.util.List;

/**
 * Shared Testcontainers infrastructure for end-to-end tests against the packaged application jar.
 */
@Testcontainers
public abstract class E2eBaseTest {

    protected static final Network NETWORK = Network.newNetwork();

    @Container
    protected static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
            .withDatabaseName("edusuite")
            .withUsername("postgres")
            .withPassword("postgres")
            .withNetwork(NETWORK)
            .withNetworkAliases("postgres");

    @Container
    protected static final GenericContainer<?> KEYCLOAK = new GenericContainer<>(DockerImageName.parse("quay.io/keycloak/keycloak:25.0"))
            .withCommand("start-dev", "--import-realm")
            .withCopyFileToContainer(
                    MountableFile.forHostPath("../keycloak/realm-export.json"),
                    "/opt/keycloak/data/import/realm-export.json"
            )
            .withNetwork(NETWORK)
            .withNetworkAliases("keycloak")
            .withEnv("KC_HTTP_ENABLED", "true")
            .withEnv("KC_HOSTNAME", "http://keycloak:8080")
            .withEnv("KC_HOSTNAME_ADMIN", "http://keycloak:8080")
            .withEnv("KC_HOSTNAME_STRICT", "false")
            .withEnv("KEYCLOAK_ADMIN", "admin")
            .withEnv("KEYCLOAK_ADMIN_PASSWORD", "admin")
            .withExposedPorts(8080)
            .waitingFor(Wait.forHttp("/realms/edusuite").forStatusCode(200).withStartupTimeout(Duration.ofMinutes(3)));

    @Container
    protected static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withNetwork(NETWORK)
            .withNetworkAliases("redis")
            .withExposedPorts(6379)
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    @Container
    protected static final GenericContainer<?> APP = new GenericContainer<>(DockerImageName.parse("eclipse-temurin:26-jre-alpine"))
            .withCopyFileToContainer(
                    MountableFile.forHostPath("../target/edusuite-platform-0.1.0-SNAPSHOT.jar"),
                    "/app.jar"
            )
            .withCommand("java", "-jar", "/app.jar")
            .withNetwork(NETWORK)
            .withNetworkAliases("app")
            .withExposedPorts(8080)
            .withEnv("SPRING_DATASOURCE_URL", "jdbc:postgresql://postgres:5432/edusuite")
            .withEnv("SPRING_DATASOURCE_USERNAME", "postgres")
            .withEnv("SPRING_DATASOURCE_PASSWORD", "postgres")
            .withEnv("SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_ISSUER_URI", "http://keycloak:8080/realms/edusuite")
            .withEnv("SERVER_PORT", "8080")
            .withLogConsumer((OutputFrame log) -> System.out.print(log.getUtf8String()))
            .dependsOn(List.<Startable>of(POSTGRES, KEYCLOAK, REDIS))
            .waitingFor(Wait.forHttp("/actuator/health")
                    .forResponsePredicate(response -> response.contains("\"status\":\"UP\""))
                    .withStartupTimeout(Duration.ofMinutes(3)));

    @BeforeAll
    static void configureRestAssured() throws Exception {
        seedTenants();
        RestAssured.baseURI = baseUrl();
    }

    private static void seedTenants() throws Exception {
        // Flyway has already run in the app container by the time tests start,
        // so the tenant table exists. Insert rows matching the Keycloak test realm.
        try (var conn = POSTGRES.createConnection("");
             var stmt = conn.createStatement()) {
            stmt.execute("""
                    INSERT INTO tenant (id, name, subdomain, institute_type, plan_tier, billing_status, locale, currency, timezone, created_at)
                    VALUES
                        ('00000000-0000-0000-0000-000000000001'::uuid, 'Tenant A', 'tenant-a', 'SCHOOL',  'STARTER', 'TRIAL', 'en-IN', 'INR', 'Asia/Kolkata', now()),
                        ('00000000-0000-0000-0000-000000000002'::uuid, 'Tenant B', 'tenant-b', 'COLLEGE', 'STARTER', 'TRIAL', 'en-IN', 'INR', 'Asia/Kolkata', now())
                    ON CONFLICT (id) DO NOTHING;
                    """);
        }
    }

    protected static String baseUrl() {
        return "http://" + APP.getHost() + ":" + APP.getMappedPort(8080);
    }

    protected String obtainAccessToken(String username, String password) {
        String tokenUrl = "http://" + KEYCLOAK.getHost() + ":" + KEYCLOAK.getMappedPort(8080)
                + "/realms/edusuite/protocol/openid-connect/token";
        Response response = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "edusuite-app")
                .formParam("scope", "tenant")
                .formParam("username", username)
                .formParam("password", password)
                .when()
                .post(tokenUrl)
                .then()
                .statusCode(200)
                .extract()
                .response();
        return response.jsonPath().getString("access_token");
    }
}
