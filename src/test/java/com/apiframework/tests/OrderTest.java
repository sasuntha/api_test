package com.apiframework.tests;

import com.apiframework.base.BaseTest;
import com.apiframework.endpoints.APIEndpoints;
import com.apiframework.models.OrderRequest;
import com.apiframework.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Test suite for the FakeStore /carts (Orders) endpoint.
 * Covers: GET all carts, GET by ID, POST, PUT, DELETE,
 *         date range filter, limit, schema validation, cart products.
 */
public class OrderTest extends BaseTest {

    private static final int EXISTING_CART_ID = 1;

    // ── TC-O-01: GET all carts ──────────────────────────────────────────────

    @Test(description = "GET /carts should return HTTP 200 and a non-empty list")
    public void test_GetAllCarts_Returns200() {
        Response response = authenticatedRequest()
                .when()
                .get(APIEndpoints.CARTS);

        logToReport("GET", APIEndpoints.CARTS, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<?> carts = response.jsonPath().getList("$");
        assertNotNull(carts, "Carts list should not be null");
        assertFalse(carts.isEmpty(), "Carts list should not be empty");

        reportManager.getCurrentTest().info("Total carts returned: " + carts.size());
        log.info("GET /carts returned {} carts", carts.size());
    }

    // ── TC-O-02: GET cart by ID ─────────────────────────────────────────────

    @Test(description = "GET /carts/{id} should return the correct cart with HTTP 200")
    public void test_GetCartById_Returns200() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_CART_ID)
                .when()
                .get(APIEndpoints.CART_BY_ID);

        logToReport("GET", "/carts/" + EXISTING_CART_ID, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        int returnedId = response.jsonPath().getInt("id");
        assertEquals(returnedId, EXISTING_CART_ID, "Returned cart ID must match");

        int userId = response.jsonPath().getInt("userId");
        assertTrue(userId > 0, "Cart should reference a valid user ID");

        List<?> products = response.jsonPath().getList("products");
        assertNotNull(products, "Cart products list should not be null");

        reportManager.getCurrentTest().info(
                "Cart id=" + returnedId + ", userId=" + userId + ", products=" + products.size());
    }

    // ── TC-O-03: Schema validation for cart response ────────────────────────

    @Test(description = "Cart response matches order_schema.json")
    public void test_GetCartById_MatchesSchema() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_CART_ID)
                .when()
                .get(APIEndpoints.CART_BY_ID);

        logToReport("GET", "/carts/" + EXISTING_CART_ID, null, response);

        assertEquals(response.getStatusCode(), 200);
        SchemaValidator.validate(response, "order_schema.json");
        reportManager.getCurrentTest().info("Schema validation passed for cart response.");
    }

    // ── TC-O-04: GET carts with limit ───────────────────────────────────────

    @Test(description = "GET /carts?limit=3 should return at most 3 carts")
    public void test_GetCartsWithLimit_ReturnsCorrectCount() {
        int limit = 3;
        Response response = authenticatedRequest()
                .queryParam("limit", limit)
                .when()
                .get(APIEndpoints.CARTS);

        logToReport("GET", APIEndpoints.CARTS + "?limit=" + limit, null, response);

        assertEquals(response.getStatusCode(), 200);

        List<?> carts = response.jsonPath().getList("$");
        assertTrue(carts.size() <= limit,
                "Should return at most " + limit + " carts, got " + carts.size());

        reportManager.getCurrentTest().info("Limit=" + limit + " applied. Returned: " + carts.size());
    }

    // ── TC-O-05: GET carts in date range ────────────────────────────────────

    @Test(description = "GET /carts with date range should return carts in that range")
    public void test_GetCartsInDateRange_Returns200() {
        String startDate = "2019-12-10";
        String endDate   = "2020-10-10";

        Response response = authenticatedRequest()
                .queryParam("startdate", startDate)
                .queryParam("enddate", endDate)
                .when()
                .get(APIEndpoints.CARTS);

        logToReport("GET",
                APIEndpoints.CARTS + "?startdate=" + startDate + "&enddate=" + endDate,
                null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<?> carts = response.jsonPath().getList("$");
        assertNotNull(carts, "Filtered carts should not be null");

        reportManager.getCurrentTest().info(
                "Carts in range [" + startDate + " → " + endDate + "]: " + carts.size());
    }

    // ── TC-O-06: POST — create a new cart/order ──────────────────────────────

    @Test(description = "POST /carts should create a cart and return HTTP 200 with an ID")
    public void test_CreateCart_Returns200WithId() {
        OrderRequest newCart = OrderRequest.sample();

        Response response = authenticatedRequest()
                .body(newCart)
                .when()
                .post(APIEndpoints.CARTS);

        logToReport("POST", APIEndpoints.CARTS, "OrderRequest.sample()", response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 on cart creation, got: " + status);

        int createdId = response.jsonPath().getInt("id");
        assertTrue(createdId > 0, "Created cart should have a positive ID");

        reportManager.getCurrentTest().info("Cart (order) created with id: " + createdId);
        log.info("POST /carts created cart with id={}", createdId);
    }

    // ── TC-O-07: PUT — update an existing cart ──────────────────────────────

    @Test(description = "PUT /carts/{id} should update the cart and return HTTP 200")
    public void test_UpdateCart_Returns200() {
        OrderRequest updated = OrderRequest.sample();
        updated.setDate("2024-06-14");

        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_CART_ID)
                .body(updated)
                .when()
                .put(APIEndpoints.CART_BY_ID);

        logToReport("PUT", "/carts/" + EXISTING_CART_ID, "Updated cart payload", response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 on cart update, got: " + status);

        Object idObj = response.jsonPath().get("id");
        if (idObj != null) {
            int returnedId = ((Number) idObj).intValue();
            assertEquals(returnedId, EXISTING_CART_ID, "Response ID should match updated cart ID");
        }

        reportManager.getCurrentTest().info("Cart id=" + EXISTING_CART_ID + " updated successfully.");
    }

    // ── TC-O-08: DELETE — remove a cart ─────────────────────────────────────

    @Test(description = "DELETE /carts/{id} should return HTTP 200")
    public void test_DeleteCart_Returns200() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_CART_ID)
                .when()
                .delete(APIEndpoints.CART_BY_ID);

        logToReport("DELETE", "/carts/" + EXISTING_CART_ID, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 on cart deletion");
        reportManager.getCurrentTest().info("Cart id=" + EXISTING_CART_ID + " deleted (mock).");
    }

    // ── TC-O-09: Cart products have valid productId and quantity ─────────────

    @Test(description = "All products in a cart must have positive productId and quantity")
    public void test_CartProducts_HaveValidFields() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_CART_ID)
                .when()
                .get(APIEndpoints.CART_BY_ID);

        logToReport("GET", "/carts/" + EXISTING_CART_ID, null, response);

        assertEquals(response.getStatusCode(), 200);

        List<Integer> productIds = response.jsonPath().getList("products.productId", Integer.class);
        List<Integer> quantities = response.jsonPath().getList("products.quantity",  Integer.class);

        assertFalse(productIds.isEmpty(), "Cart should contain at least one product");

        for (int i = 0; i < productIds.size(); i++) {
            assertTrue(productIds.get(i) > 0, "productId at index " + i + " should be positive");
            assertTrue(quantities.get(i) > 0,  "quantity at index "  + i + " should be positive");
        }

        reportManager.getCurrentTest().info(
                "Validated " + productIds.size() + " products in cart id=" + EXISTING_CART_ID);
    }

    // ── TC-O-10: GET non-existent cart returns 404 ──────────────────────────

    @Test(description = "GET /carts/99999 for non-existent cart returns 404 or null body")
    public void test_GetNonExistentCart_Returns404() {
        Response response = authenticatedRequest()
                .pathParam("id", 99999)
                .when()
                .get(APIEndpoints.CART_BY_ID);

        logToReport("GET", "/carts/99999", null, response);

        int status = response.getStatusCode();
        // FakeStore mock API returns 404 OR 200 with "null" body for missing resources
        boolean isNotFound = status == 404
                || (status == 200 && "null".equals(response.getBody().asString().trim()));
        assertTrue(isNotFound,
                "Expected 404 or null body for non-existent cart, got status=" + status
                        + " body=" + response.getBody().asString());
        reportManager.getCurrentTest().info("Non-existent cart handled correctly (HTTP " + status + ").");
    }
}
