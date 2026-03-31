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
    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;


    @Column(nullable = false)
    private double price;

    @Column(name = "price_at_addition", nullable = false)
    private double priceAtAddition;


    public CartItem() {}

    public CartItem(Cart cart, Product product, Integer quantity, double price, double priceAtAddition) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
        this.price = price;
        this.priceAtAddition = priceAtAddition;
    }

    // Getters & Setters
    public Long getId() {
        return id;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getPriceAtAddition() {
        return priceAtAddition;
    }

    public void setPriceAtAddition(double priceAtAddition) {
        this.priceAtAddition = priceAtAddition;
    }
}