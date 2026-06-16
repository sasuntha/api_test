package com.apiframework.tests;

import com.apiframework.base.BaseTest;
import com.apiframework.endpoints.APIEndpoints;
import com.apiframework.models.ProductRequest;
import com.apiframework.utils.SchemaValidator;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.*;

/**
 * Test suite for the FakeStore /products endpoint.
 * Covers: GET all, GET by ID, GET categories, GET by category,
 *         POST, PUT, DELETE, limit/sort query params, schema validation.
 */
public class ProductTest extends BaseTest {

    private static final int EXISTING_PRODUCT_ID = 1;

    // ── TC-P-01: GET all products ───────────────────────────────────────────

    @Test(description = "GET /products should return HTTP 200 and a non-empty list")
    public void test_GetAllProducts_Returns200() {
        Response response = unauthenticatedRequest()
                .when()
                .get(APIEndpoints.PRODUCTS);

        logToReport("GET", APIEndpoints.PRODUCTS, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<?> products = response.jsonPath().getList("$");
        assertNotNull(products, "Products list should not be null");
        assertFalse(products.isEmpty(), "Products list should not be empty");

        reportManager.getCurrentTest().info("Total products returned: " + products.size());
    }

    // ── TC-P-02: GET product by ID ──────────────────────────────────────────

    @Test(description = "GET /products/{id} should return the correct product")
    public void test_GetProductById_Returns200() {
        Response response = unauthenticatedRequest()
                .pathParam("id", EXISTING_PRODUCT_ID)
                .when()
                .get(APIEndpoints.PRODUCT_BY_ID);

        logToReport("GET", "/products/" + EXISTING_PRODUCT_ID, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        int returnedId = response.jsonPath().getInt("id");
        assertEquals(returnedId, EXISTING_PRODUCT_ID);

        String title = response.jsonPath().getString("title");
        assertNotNull(title, "Product title should be present");
        assertFalse(title.isBlank(), "Product title should not be blank");

        double price = response.jsonPath().getDouble("price");
        assertTrue(price > 0, "Product price should be positive");

        reportManager.getCurrentTest().info("Product: id=" + returnedId + ", title=" + title + ", price=$" + price);
    }

    // ── TC-P-03: Schema validation for product response ─────────────────────

    @Test(description = "Product response matches product_schema.json")
    public void test_GetProductById_MatchesSchema() {
        Response response = unauthenticatedRequest()
                .pathParam("id", EXISTING_PRODUCT_ID)
                .when()
                .get(APIEndpoints.PRODUCT_BY_ID);

        logToReport("GET", "/products/" + EXISTING_PRODUCT_ID, null, response);

        assertEquals(response.getStatusCode(), 200);
        SchemaValidator.validate(response, "product_schema.json");
        reportManager.getCurrentTest().info("Schema validation passed for product response.");
    }

    // ── TC-P-04: GET products with limit ────────────────────────────────────

    @Test(description = "GET /products?limit=5 should return exactly 5 products")
    public void test_GetProductsWithLimit_ReturnsCorrectCount() {
        int limit = 5;
        Response response = unauthenticatedRequest()
                .queryParam("limit", limit)
                .when()
                .get(APIEndpoints.PRODUCTS);

        logToReport("GET", APIEndpoints.PRODUCTS + "?limit=" + limit, null, response);

        assertEquals(response.getStatusCode(), 200);

        List<?> products = response.jsonPath().getList("$");
        assertEquals(products.size(), limit, "Should return exactly " + limit + " products");
        reportManager.getCurrentTest().info("Limit=" + limit + " applied, got " + products.size() + " products.");
    }

    // ── TC-P-05: GET products sorted descending ──────────────────────────────

    @Test(description = "GET /products?sort=desc should return products in descending ID order")
    public void test_GetProductsSortedDesc() {
        Response response = unauthenticatedRequest()
                .queryParam("sort", "desc")
                .when()
                .get(APIEndpoints.PRODUCTS);

        logToReport("GET", APIEndpoints.PRODUCTS + "?sort=desc", null, response);

        assertEquals(response.getStatusCode(), 200);

        List<Integer> ids = response.jsonPath().getList("id", Integer.class);
        assertFalse(ids.isEmpty());

        for (int i = 0; i < ids.size() - 1; i++) {
            assertTrue(ids.get(i) >= ids.get(i + 1),
                    "IDs should be descending at index " + i);
        }
        reportManager.getCurrentTest().info("Descending sort verified. First id=" + ids.get(0));
    }

    // ── TC-P-06: GET all categories ─────────────────────────────────────────

    @Test(description = "GET /products/categories should return the categories list")
    public void test_GetAllCategories_Returns200() {
        Response response = unauthenticatedRequest()
                .when()
                .get(APIEndpoints.PRODUCT_CATEGORIES);

        logToReport("GET", APIEndpoints.PRODUCT_CATEGORIES, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<String> categories = response.jsonPath().getList("$", String.class);
        assertNotNull(categories);
        assertFalse(categories.isEmpty(), "Categories list should not be empty");

        reportManager.getCurrentTest().info("Categories: " + categories);
        log.info("Available product categories: {}", categories);
    }

    // ── TC-P-07: GET products by category ───────────────────────────────────

    @Test(description = "GET /products/category/electronics should return electronics only",
          dependsOnMethods = "test_GetAllCategories_Returns200")
    public void test_GetProductsByCategory_ReturnsCorrectCategory() {
        String category = "electronics";

        Response response = unauthenticatedRequest()
                .pathParam("category", category)
                .when()
                .get(APIEndpoints.PRODUCTS_BY_CATEGORY);

        logToReport("GET", "/products/category/" + category, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200");

        List<String> returnedCategories = response.jsonPath().getList("category", String.class);
        assertFalse(returnedCategories.isEmpty(), "Should return at least one product");

        for (String cat : returnedCategories) {
            assertEquals(cat, category, "All products should be in category: " + category);
        }
        reportManager.getCurrentTest().info("All " + returnedCategories.size() + " returned products are in category: " + category);
    }

    // ── TC-P-08: POST — create a new product ────────────────────────────────

    @Test(description = "POST /products should create a product and return HTTP 200 with an ID")
    public void test_CreateProduct_Returns200WithId() {
        ProductRequest newProduct = ProductRequest.sample();

        Response response = authenticatedRequest()
                .body(newProduct)
                .when()
                .post(APIEndpoints.PRODUCTS);

        logToReport("POST", APIEndpoints.PRODUCTS, "ProductRequest.sample()", response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 on product creation, got: " + status);

        int createdId = response.jsonPath().getInt("id");
        assertTrue(createdId > 0, "Created product should have a positive ID");

        reportManager.getCurrentTest().info("Product created with id: " + createdId);
    }

    // ── TC-P-09: PUT — update a product ─────────────────────────────────────

    @Test(description = "PUT /products/{id} should update and return HTTP 200")
    public void test_UpdateProduct_Returns200() {
        ProductRequest updated = ProductRequest.sample();
        updated.setTitle("Updated Automation Product");
        updated.setPrice(79.99);

        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_PRODUCT_ID)
                .body(updated)
                .when()
                .put(APIEndpoints.PRODUCT_BY_ID);

        logToReport("PUT", "/products/" + EXISTING_PRODUCT_ID, "Updated product payload", response);

        int status = response.getStatusCode();
        assertTrue(status == 200 || status == 201,
                "Expected HTTP 200/201 on product update, got: " + status);

        Object idObj = response.jsonPath().get("id");
        if (idObj != null) {
            int returnedId = ((Number) idObj).intValue();
            assertEquals(returnedId, EXISTING_PRODUCT_ID);
        }

        reportManager.getCurrentTest().info("Product id=" + EXISTING_PRODUCT_ID + " updated successfully.");
    }

    // ── TC-P-10: DELETE — remove a product ──────────────────────────────────

    @Test(description = "DELETE /products/{id} should return HTTP 200")
    public void test_DeleteProduct_Returns200() {
        Response response = authenticatedRequest()
                .pathParam("id", EXISTING_PRODUCT_ID)
                .when()
                .delete(APIEndpoints.PRODUCT_BY_ID);

        logToReport("DELETE", "/products/" + EXISTING_PRODUCT_ID, null, response);

        assertEquals(response.getStatusCode(), 200, "Expected HTTP 200 on product deletion");
        reportManager.getCurrentTest().info("Product id=" + EXISTING_PRODUCT_ID + " deleted (mock).");
    }

    // ── TC-P-11: Product price field is a positive number ──────────────────

    @Test(description = "All products must have a price greater than zero")
    public void test_AllProducts_HavePositivePrice() {
        Response response = unauthenticatedRequest()
                .when()
                .get(APIEndpoints.PRODUCTS);

        logToReport("GET", APIEndpoints.PRODUCTS, null, response);

        assertEquals(response.getStatusCode(), 200);

        List<Double> prices = response.jsonPath().getList("price", Double.class);
        for (int i = 0; i < prices.size(); i++) {
            assertTrue(prices.get(i) > 0,
                    "Product at index " + i + " has non-positive price: " + prices.get(i));
        }
        reportManager.getCurrentTest().info("All " + prices.size() + " products have positive prices.");
    }
}
