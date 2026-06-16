package com.apiframework.utils;

import com.apiframework.config.ConfigManager;
import com.apiframework.endpoints.APIEndpoints;
import io.restassured.response.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;

/**
 * Manages authentication token lifecycle for the test suite.
 * Token is fetched once per test run and cached for reuse.
 */
public class AuthManager {

    private static final Logger log = LoggerFactory.getLogger(AuthManager.class);
    private static AuthManager instance;
    private String cachedToken;

    private AuthManager() {}

    public static synchronized AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    /**
     * Returns a valid JWT token, fetching a new one from the API if needed.
     */
    public String getToken() {
        if (cachedToken == null || cachedToken.isBlank()) {
            cachedToken = fetchToken();
        }
        return cachedToken;
    }

    /** Forces re-authentication and refreshes the cached token. */
    public String refreshToken() {
        cachedToken = null;
        return getToken();
    }

    /** Returns the Authorization header value ready for use. */
    public String getBearerToken() {
        return "Bearer " + getToken();
    }

    private String fetchToken() {
        ConfigManager config = ConfigManager.getInstance();
        Map<String, String> credentials = new HashMap<>();
        credentials.put("username", config.getUsername());
        credentials.put("password", config.getPassword());

        log.info("Fetching authentication token for user: {}", config.getUsername());

        Response response = given()
                .baseUri(config.getBaseUrl())
                .contentType("application/json")
                .body(credentials)
                .when()
                .post(APIEndpoints.LOGIN)
                .then()
                .statusCode(org.hamcrest.Matchers.anyOf(
                        org.hamcrest.Matchers.is(200),
                        org.hamcrest.Matchers.is(201)))
                .extract()
                .response();

        String token = response.jsonPath().getString("token");
        log.info("Authentication token fetched successfully (length={})", token.length());
        return token;
    }

    public void invalidateToken() {
        cachedToken = null;
        log.info("Cached authentication token invalidated.");
    }
}
