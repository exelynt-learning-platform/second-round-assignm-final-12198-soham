package com.mjs.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

@Entity
@Table(name = "cart_items")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many cart items belong to one cart
    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false)
    @NotNull(message = "Cart is required")
    private Cart cart;

    // Each item refers to one product
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    @NotNull(message = "Product is required")
    private Product product;

    // Quantity must be at least 1

    @Min(value = 1, message = "Quantity must be at least 1")
    @NotNull(message = "Quantity is required")
    private Integer quantity;


    @Column(nullable = false)
    @NotNull
    private double price;


    public CartItem(Cart cart, Product product, int quantity,double price) {
    }
}