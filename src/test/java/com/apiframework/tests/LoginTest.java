package com.apiframework.tests;

import com.apiframework.base.BaseTest;
import com.apiframework.endpoints.APIEndpoints;
import com.apiframework.models.LoginRequest;
import com.apiframework.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

/**
 * Test suite for the FakeStore /auth/login endpoint.
 * Covers: successful login, invalid credentials, missing fields, schema validation.
 */
public class LoginTest extends BaseTest {

    // ── TC-L-01: Successful login with valid credentials ──────────────────────

    @Test(description = "Valid credentials return HTTP 200 and a non-empty token")
    public void test_ValidLogin_Returns200AndToken() {
        LoginRequest payload = new LoginRequest(config.getUsername(), config.getPassword());

        Response response = unauthenticatedRequest()
                .body(payload)
                .when()
                .post(APIEndpoints.LOGIN);

        logToReport("POST", APIEndpoints.LOGIN, payload.toString(), response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 for valid credentials, got: " + status);

        String token = response.jsonPath().getString("token");
        assertNotNull(token, "Token should not be null");
        assertFalse(token.isBlank(), "Token should not be empty");

        reportManager.getCurrentTest().info("Token received (length=" + token.length() + ")");
        log.info("Login token acquired. Length={}", token.length());
    }

    // ── TC-L-02: Login response matches JSON schema ────────────────────────────

    @Test(description = "Login response body matches login_schema.json",
          dependsOnMethods = "test_ValidLogin_Returns200AndToken")
    public void test_LoginResponse_MatchesSchema() {
        LoginRequest payload = new LoginRequest(config.getUsername(), config.getPassword());

        Response response = unauthenticatedRequest()
                .body(payload)
                .when()
                .post(APIEndpoints.LOGIN);

        logToReport("POST", APIEndpoints.LOGIN, payload.toString(), response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 for valid credentials, got: " + status);
        SchemaValidator.validate(response, "login_schema.json");
        reportManager.getCurrentTest().info("JSON schema validation passed.");
    }

    // ── TC-L-03: Login with wrong password returns 401 ────────────────────────

    @Test(description = "Wrong password should return HTTP 401")
    public void test_WrongPassword_Returns401() {
        LoginRequest payload = new LoginRequest(config.getUsername(), "wrongpassword!");

        Response response = unauthenticatedRequest()
                .body(payload)
                .when()
                .post(APIEndpoints.LOGIN);

        logToReport("POST", APIEndpoints.LOGIN, payload.toString(), response);

        int status = response.getStatusCode();
        assertTrue(status == 400 || status == 401,
                "Expected HTTP 400/401 for wrong password, got: " + status);
        reportManager.getCurrentTest().info("Correct rejection for wrong password (HTTP " + status + ").");
    }

    // ── TC-L-04: Login with non-existent username returns 401 ─────────────────

    @Test(description = "Non-existent username should return HTTP 401")
    public void test_NonExistentUser_Returns401() {
        LoginRequest payload = new LoginRequest("nobody_99999", "somepassword");

        Response response = unauthenticatedRequest()
                .body(payload)
                .when()
                .post(APIEndpoints.LOGIN);

        logToReport("POST", APIEndpoints.LOGIN, payload.toString(), response);

        int status = response.getStatusCode();
        assertTrue(status == 400 || status == 401,
                "Expected HTTP 400/401 for non-existent user, got: " + status);
    }

    // ── TC-L-05: Response time is within acceptable threshold ─────────────────

    @Test(description = "Login response time should be under 5 seconds")
    public void test_LoginResponseTime_IsAcceptable() {
        LoginRequest payload = new LoginRequest(config.getUsername(), config.getPassword());

        Response response = unauthenticatedRequest()
                .body(payload)
                .when()
                .post(APIEndpoints.LOGIN);

        logToReport("POST", APIEndpoints.LOGIN, payload.toString(), response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected 200/201, got: " + status);
        long responseTime = response.getTime();
        assertTrue(responseTime < 5000,
                "Response time " + responseTime + "ms exceeded 5000ms threshold");

        reportManager.getCurrentTest().info("Response time: " + responseTime + " ms");
        log.info("Login response time: {} ms", responseTime);
    }

    // ── TC-L-06: Token is reused across requests (AuthManager caching) ─────────

    @Test(description = "AuthManager returns the same token on repeated calls (caching)",
          dependsOnMethods = "test_ValidLogin_Returns200AndToken")
    public void test_TokenCaching_ReturnsSameToken() {
        String token1 = authManager.getToken();
        String token2 = authManager.getToken();
        assertEquals(token1, token2, "AuthManager should return cached token without re-authenticating");
        reportManager.getCurrentTest().info("Token caching verified. Both calls returned identical token.");
    }
}
