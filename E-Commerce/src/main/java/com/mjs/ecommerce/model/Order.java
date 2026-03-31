package com.mjs.ecommerce.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;

import java.util.List;

@Entity
@Table(name = "orders")
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @PositiveOrZero(message = "Total price must be valid")
    private double totalPrice;

    @NotBlank(message = "Status required")
    private String status;

    @NotBlank(message = "Payment status required")
    private String paymentStatus;

    @NotBlank(message = "Shipping address required")
    private String shippingAddress;

    @OneToMany(mappedBy ="order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @PositiveOrZero(message = "Total price must be valid")
    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(@PositiveOrZero(message = "Total price must be valid") double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public @NotBlank(message = "Status required") String getStatus() {
        return status;
    }

    public void setStatus(@NotBlank(message = "Status required") String status) {
        this.status = status;
    }

    public @NotBlank(message = "Payment status required") String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(@NotBlank(message = "Payment status required") String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public @NotBlank(message = "Shipping address required") String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(@NotBlank(message = "Shipping address required") String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
}