package com.mjs.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Product name required")
    private String name;

    @NotBlank(message = "Description required")
    private String description;

    @Positive(message = "Price must be positive")
    private double price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    @NotBlank(message = "Image URL required")
    private String imageUrl;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotBlank(message = "Product name required") String getName() {
        return name;
    }

    public void setName(@NotBlank(message = "Product name required") String name) {
        this.name = name;
    }

    public @NotBlank(message = "Description required") String getDescription() {
        return description;
    }

    public void setDescription(@NotBlank(message = "Description required") String description) {
        this.description = description;
    }

    @Positive(message = "Price must be positive")
    public double getPrice() {
        return price;
    }

    public void setPrice(@Positive(message = "Price must be positive") double price) {
        this.price = price;
    }

    @Min(value = 0, message = "Stock cannot be negative")
    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(@Min(value = 0, message = "Stock cannot be negative") int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public @NotBlank(message = "Image URL required") String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(@NotBlank(message = "Image URL required") String imageUrl) {
        this.imageUrl = imageUrl;
    }
}