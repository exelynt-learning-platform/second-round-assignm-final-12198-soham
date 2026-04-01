package com.mjs.ecommerce.model;

import com.mjs.ecommerce.enums.OrderStatus;
import com.mjs.ecommerce.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "orders")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Order{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private User user;

    @PositiveOrZero(message = "Total price must be valid")
    private double totalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus;

    @NotBlank(message = "Shipping address required")
    private String shippingAddress;

    @OneToMany(mappedBy ="order", cascade = CascadeType.ALL)
    private List<OrderItem> items;


}