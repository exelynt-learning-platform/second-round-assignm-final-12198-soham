package com.mjs.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "cart_items")
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


    public CartItem() {
    }

    public CartItem(Cart cart, Product product, Integer quantity, double price) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public @NotNull(message = "Cart is required") Cart getCart() {
        return cart;
    }

    public void setCart(@NotNull(message = "Cart is required") Cart cart) {
        this.cart = cart;
    }

    public @NotNull(message = "Product is required") Product getProduct() {
        return product;
    }

    public void setProduct(@NotNull(message = "Product is required") Product product) {
        this.product = product;
    }

    public @Min(value = 1, message = "Quantity must be at least 1") @NotNull(message = "Quantity is required") Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(@Min(value = 1, message = "Quantity must be at least 1") @NotNull(message = "Quantity is required") Integer quantity) {
        this.quantity = quantity;
    }

    @NotNull
    public double getPrice() {
        return price;
    }

    public void setPrice(@NotNull double price) {
        this.price = price;
    }
}