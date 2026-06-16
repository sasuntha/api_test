package com.apiframework.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductRequest {

    @JsonProperty("title")
    private String title;

    @JsonProperty("price")
    private double price;

    @JsonProperty("description")
    private String description;

    @JsonProperty("category")
    private String category;

    @JsonProperty("image")
    private String image;

    public ProductRequest() {}

    public ProductRequest(String title, double price, String description,
                          String category, String image) {
        this.title = title;
        this.price = price;
        this.description = description;
        this.category = category;
        this.image = image;
    }

    public String getTitle()       { return title; }
    public void setTitle(String t) { this.title = t; }

    public double getPrice()        { return price; }
    public void setPrice(double p)  { this.price = p; }

    public String getDescription()        { return description; }
    public void setDescription(String d)  { this.description = d; }

    public String getCategory()        { return category; }
    public void setCategory(String c)  { this.category = c; }

    public String getImage()       { return image; }
    public void setImage(String i) { this.image = i; }

    /** Factory: builds a sample product payload for POST/PUT tests. */
    public static ProductRequest sample() {
        return new ProductRequest(
                "Automation Test Product",
                49.99,
                "A product created by the automation framework for testing purposes.",
                "electronics",
                "https://fakestoreapi.com/img/placeholder.jpg"
        );
    }
}
