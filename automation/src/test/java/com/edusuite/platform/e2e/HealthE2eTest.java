package com.edusuite.platform.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HealthE2eTest extends E2eBaseTest {

    @Test
    void actuatorHealthReturnsUp() {
        String status = RestAssured.given()
                .when()
                .get("/actuator/health")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getString("status");

        assertThat(status).isEqualTo("UP");
    }
}
