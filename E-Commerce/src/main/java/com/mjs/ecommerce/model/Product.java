package com.mjs.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
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


}