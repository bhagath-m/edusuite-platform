package com.edusuite.platform.e2e;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;

class AuthE2eTest extends E2eBaseTest {

    @Test
    void authenticatedRequestWithValidTokenSucceeds() {
        String token = obtainAccessToken("admin@tenant-a.test", "password123");

        RestAssured.given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/api/v1/audit")
                .then()
                .statusCode(200);
    }

    @Test
    void authenticatedRequestWithoutTokenReturnsUnauthorized() {
        RestAssured.given()
                .when()
                .get("/api/v1/audit")
                .then()
                .statusCode(401);
    }
}
