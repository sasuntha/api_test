package com.apiframework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

    @JsonProperty("userId")
    private int userId;

    @JsonProperty("date")
    private String date;

    @JsonProperty("products")
    private List<OrderItem> products;

    public OrderRequest() {}

    public OrderRequest(int userId, String date, List<OrderItem> products) {
        this.userId = userId;
        this.date = date;
        this.products = products;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class OrderItem {
        @JsonProperty("productId") private int productId;
        @JsonProperty("quantity")  private int quantity;

        public OrderItem() {}
        public OrderItem(int productId, int quantity) {
            this.productId = productId;
            this.quantity = quantity;
        }
        public int getProductId() { return productId; }
        public void setProductId(int productId) { this.productId = productId; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
    }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public List<OrderItem> getProducts() { return products; }
    public void setProducts(List<OrderItem> products) { this.products = products; }

    /** Factory: builds a sample cart (order) for POST/PUT tests. */
    public static OrderRequest sample() {
        return new OrderRequest(
                5,
                "2024-01-15",
                Arrays.asList(
                        new OrderItem(5, 1),
                        new OrderItem(1, 3)
                )
        );
    }
}
