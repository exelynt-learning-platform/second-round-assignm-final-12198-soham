package com.mjs.ecommerce.mapper;


import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PaymentMapperTest {

    private PaymentMapper paymentMapper;

    @BeforeEach
    void setUp() {
        paymentMapper = new PaymentMapper();
    }

    // =========================
    // CREATE PAYMENT SUCCESS
    // =========================
    @Test
    void createPayment_shouldMapAllFieldsCorrectly() {

        // Arrange
        User user = new User();
        user.setId(1L);

        Order order = new Order();
        order.setId(10L);
        order.setUser(user);

        PaymentRequest request = new PaymentRequest();
        request.setAmount(500.0);
        request.setCurrency("INR");
        request.setPaymentMethodToken("pm_123");
        request.setDescription("Test payment");

        String stripeId = "pi_123";

        // Act
        Payment payment = paymentMapper.createPayment(user, order, request, stripeId);

        // Assert
        assertNotNull(payment);
        assertEquals(user, payment.getUser());
        assertEquals(order, payment.getOrder());
        assertEquals(500.0, payment.getAmount());
        assertEquals("INR", payment.getCurrency());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());
        assertEquals("pm_123", payment.getPaymentMethod());
        assertEquals("pi_123", payment.getStripePaymentIntentId());
        assertEquals("Test payment", payment.getDescription());

        assertNotNull(payment.getCreatedAt());
        assertNotNull(payment.getUpdatedAt());
    }

    // =========================
    // NULL INPUT SAFETY TEST
    // =========================
    @Test
    void createPayment_shouldHandleNullFieldsGracefully() {

        // Arrange
        User user = new User();
        Order order = new Order();
        PaymentRequest request = new PaymentRequest();

        // Act
        Payment payment = paymentMapper.createPayment(user, order, request, "pi_test");

        // Assert
        assertNotNull(payment);
        assertEquals(user, payment.getUser());
        assertEquals(order, payment.getOrder());
        assertEquals(PaymentStatus.PENDING, payment.getStatus());

        // optional fields may be null
        assertNull(payment.getPaymentMethod());
        assertNull(payment.getDescription());
    }
}
