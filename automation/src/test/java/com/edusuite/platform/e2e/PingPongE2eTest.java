package com.edusuite.platform.e2e;

import io.restassured.RestAssured;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PingPongE2eTest extends E2eBaseTest {

    @Test
    void pingReturnsCorrelationIdThatEventuallyAppearsInPong() {
        String correlationId = RestAssured.given()
                .when()
                .post("/api/v1/public/ping")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("correlationId");

        assertThat(correlationId).isNotBlank();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(250))
                .untilAsserted(() -> {
                    List<String> correlationIds = RestAssured.given()
                            .when()
                            .get("/api/v1/public/pong")
                            .then()
                            .statusCode(200)
                            .extract()
                            .jsonPath()
                            .getList(".", String.class);
                    assertThat(correlationIds).contains(correlationId);
                });
    }
}
