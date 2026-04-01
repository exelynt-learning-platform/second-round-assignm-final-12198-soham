package com.mjs.ecommerce.model;

import com.mjs.ecommerce.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotBlank(message = "Stripe Payment Intent ID is required")
    @Column(unique = true, nullable = false)
    private String stripePaymentIntentId;

    @Positive(message = "Amount must be positive")
    @Column(nullable = false)
    private Double amount;

    @NotBlank(message = "Currency is required")
    @Column(nullable = false)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    @NotBlank(message = "Payment method is required")
    @Column(nullable = false)
    private String paymentMethod;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 500)
    private String failureReason;





}

