package com.apiframework.tests;

import com.apiframework.base.BaseTest;
import com.apiframework.endpoints.APIEndpoints;
import com.apiframework.models.UserRequest;
import com.apiframework.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.testng.Assert.*;

/**
 * Test suite for the FakeStore /users endpoint.
 * Covers: GET all users, GET by ID, POST (create), PUT (update), DELETE, sorting, limiting.
 */
public class UserTest extends BaseTest {

    private static final int EXISTING_USER_ID = 1;

    // ── TC-U-01: GET all users ──────────────────────────────────────────────

    @Test(description = "GET /users should return HTTP 200 and a non-empty list")
    public void test_GetAllUsers_Returns200() {
        Response response = authenticatedRequest()
                .when()
                .get(APIEndpoints.USERS);

        logToReport("GET", APIEndpoints.USERS, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<?> users = response.jsonPath().getList("$");
        assertNotNull(users, "Users list should not be null");
        assertFalse(users.isEmpty(), "Users list should not be empty");

        reportManager.getCurrentTest().info("Total users returned: " + users.size());
        log.info("GET /users returned {} users", users.size());
    }

    // ── TC-U-02: GET user by ID ─────────────────────────────────────────────

    @Test(description = "GET /users/{id} should return the correct user with HTTP 200")
    public void test_GetUserById_Returns200() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .get(APIEndpoints.USER_BY_ID);

        logToReport("GET", "/users/" + EXISTING_USER_ID, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        int returnedId = response.jsonPath().getInt("id");
        assertEquals(returnedId, EXISTING_USER_ID, "Returned user ID should match requested ID");

        String email = response.jsonPath().getString("email");
        assertNotNull(email, "Email should be present");

        reportManager.getCurrentTest().info("User retrieved: id=" + returnedId + ", email=" + email);
    }

    // ── TC-U-03: GET user by ID — schema validation ─────────────────────────

    @Test(description = "User response matches user_schema.json")
    public void test_GetUserById_MatchesSchema() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .get(APIEndpoints.USER_BY_ID);

        logToReport("GET", "/users/" + EXISTING_USER_ID, null, response);

        assertEquals(response.getStatusCode(), 200);
        SchemaValidator.validate(response, "user_schema.json");
        reportManager.getCurrentTest().info("Schema validation passed for user response.");
    }

    // ── TC-U-04: GET users with limit ───────────────────────────────────────

    @Test(description = "GET /users?limit=3 should return exactly 3 users")
    public void test_GetUsersWithLimit_ReturnsCorrectCount() {
        int limit = 3;
        Response response = authenticatedRequest()
                .queryParam("limit", limit)
                .when()
                .get(APIEndpoints.USERS);

        logToReport("GET", APIEndpoints.USERS + "?limit=" + limit, null, response);

        assertEquals(response.getStatusCode(), 200);

        List<?> users = response.jsonPath().getList("$");
        assertEquals(users.size(), limit, "Should return exactly " + limit + " users");

        reportManager.getCurrentTest().info("Limit=" + limit + " applied. Returned: " + users.size());
    }

    // ── TC-U-05: GET users sorted descending ────────────────────────────────

    @Test(description = "GET /users?sort=desc should return users in descending ID order")
    public void test_GetUsersSortedDesc_ReturnsDescendingOrder() {
        Response response = authenticatedRequest()
                .queryParam("sort", "desc")
                .when()
                .get(APIEndpoints.USERS);

        logToReport("GET", APIEndpoints.USERS + "?sort=desc", null, response);

        assertEquals(response.getStatusCode(), 200);

        List<Integer> ids = response.jsonPath().getList("id", Integer.class);
        assertFalse(ids.isEmpty(), "ID list should not be empty");

        // Verify descending order
        for (int i = 0; i < ids.size() - 1; i++) {
            assertTrue(ids.get(i) >= ids.get(i + 1),
                    "IDs should be in descending order at index " + i);
        }
        reportManager.getCurrentTest().info("Descending sort verified. IDs: " + ids);
    }

    // ── TC-U-06: POST — create a new user ───────────────────────────────────

    @Test(description = "POST /users should create a user and return HTTP 200 with an ID")
    public void test_CreateUser_Returns200WithId() {
        UserRequest newUser = UserRequest.sample();

        Response response = authenticatedRequest()
                .body(newUser)
                .when()
                .post(APIEndpoints.USERS);

        logToReport("POST", APIEndpoints.USERS, "UserRequest.sample()", response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 on user creation, got: " + status);

        int createdId = response.jsonPath().getInt("id");
        assertTrue(createdId > 0, "Created user should have a positive ID");

        reportManager.getCurrentTest().info("User created with id: " + createdId);
        log.info("POST /users created user with id={}", createdId);
    }

    // ── TC-U-07: PUT — update an existing user ──────────────────────────────

    @Test(description = "PUT /users/{id} should update the user and return HTTP 200")
    public void test_UpdateUser_Returns200() {
        UserRequest updated = UserRequest.sample();
        updated.setEmail("updated.email@example.com");
        updated.setPhone("1-800-UPDATE");

        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_USER_ID)
                .body(updated)
                .when()
                .put(APIEndpoints.USER_BY_ID);

        logToReport("PUT", "/users/" + EXISTING_USER_ID, "Updated user payload", response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 on user update, got: " + status);

        // FakeStore PUT may return id as integer or null; accept both
        Object idObj = response.jsonPath().get("id");
        if (idObj != null) {
            int returnedId = ((Number) idObj).intValue();
            assertEquals(returnedId, EXISTING_USER_ID, "Response ID should match updated user ID");
        }

        reportManager.getCurrentTest().info("User id=" + EXISTING_USER_ID + " updated successfully.");
    }

    // ── TC-U-08: DELETE — remove an existing user ───────────────────────────

    @Test(description = "DELETE /users/{id} should return HTTP 200")
    public void test_DeleteUser_Returns200() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_USER_ID)
                .when()
                .delete(APIEndpoints.USER_BY_ID);

        logToReport("DELETE", "/users/" + EXISTING_USER_ID, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 on user deletion");
        reportManager.getCurrentTest().info("User id=" + EXISTING_USER_ID + " deleted (mock).");
    }

    // ── TC-U-09: GET non-existent user returns 404 ──────────────────────────

    @Test(description = "GET /users/99999 for non-existent user returns 404 or null body")
    public void test_GetNonExistentUser_Returns404() {
        Response response = authenticatedRequest()
                .pathParam("id", 99999)
                .when()
                .get(APIEndpoints.USER_BY_ID);

        logToReport("GET", "/users/99999", null, response);

        int status = response.getStatusCode();
        // FakeStore mock API returns 404 OR 200 with "null" body for missing resources
        boolean isNotFound = status == 404
                || (status == 200 && "null".equals(response.getBody().asString().trim()));
        assertTrue(isNotFound,
                "Expected 404 or null body for non-existent user, got status=" + status
                        + " body=" + response.getBody().asString());
        reportManager.getCurrentTest().info("Non-existent user handled correctly (HTTP " + status + ").");
    }

    // ── TC-U-10: GET carts for a specific user ──────────────────────────────

    @Test(description = "GET /carts/user/{userId} should return the user's carts")
    public void test_GetCartsForUser_Returns200() {
        Response response = authenticatedRequest()
                .pathParam("userId", 2)
                .when()
                .get(APIEndpoints.USER_CARTS);

        logToReport("GET", "/carts/user/2", null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<?> carts = response.jsonPath().getList("$");
        assertNotNull(carts, "Carts list should not be null");

        reportManager.getCurrentTest().info("Carts returned for user 2: " + carts.size());
    }
}
