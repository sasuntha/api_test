package com.apiframework.endpoints;

/**
 * Central registry of all FakeStore API endpoint paths.
 * Paths are relative to the base URL configured in ConfigManager.
 */
public final class APIEndpoints {

    private APIEndpoints() {}

    // ── Authentication ───────────────────────────────────────────────────────
    public static final String LOGIN = "/auth/login";

    // ── Users ─────────────────────────────────────────────────────────────────
    public static final String USERS          = "/users";
    public static final String USER_BY_ID     = "/users/{id}";
    public static final String USERS_LIMIT    = "/users?limit={limit}";
    public static final String USERS_SORT     = "/users?sort={sort}";
    public static final String USER_CARTS     = "/carts/user/{userId}";

    // ── Products ──────────────────────────────────────────────────────────────
    public static final String PRODUCTS              = "/products";
    public static final String PRODUCT_BY_ID         = "/products/{id}";
    public static final String PRODUCTS_LIMIT        = "/products?limit={limit}";
    public static final String PRODUCTS_SORT         = "/products?sort={sort}";
    public static final String PRODUCT_CATEGORIES    = "/products/categories";
    public static final String PRODUCTS_BY_CATEGORY  = "/products/category/{category}";

    // ── Orders (Carts) ────────────────────────────────────────────────────────
    public static final String CARTS         = "/carts";
    public static final String CART_BY_ID    = "/carts/{id}";
    public static final String CARTS_LIMIT   = "/carts?limit={limit}";
    public static final String CARTS_IN_RANGE = "/carts?startdate={startdate}&enddate={enddate}";
}
