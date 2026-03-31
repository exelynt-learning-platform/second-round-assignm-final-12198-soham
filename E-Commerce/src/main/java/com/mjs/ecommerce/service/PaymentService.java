package com.mjs.ecommerce.service;

import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.model.Payment;

import java.util.List;

public interface PaymentService {

    /**
     * Create a payment intent and process the payment using Stripe
     * @param userId User ID
     * @param paymentRequest Payment details
     * @return PaymentResponse with payment status
     */
    PaymentResponse createPayment(Long userId, PaymentRequest paymentRequest);

    /**
     * Retrieve payment by ID
     * @param paymentId Payment ID
     * @return Payment object
     */
    Payment getPaymentById(Long paymentId);

    /**
     * Get all payments for a specific user
     * @param userId User ID
     * @return List of payments
     */
    List<Payment> getPaymentsByUser(Long userId);

    /**
     * Get all payments for a specific order
     * @param orderId Order ID
     * @return List of payments
     */
    List<Payment> getPaymentsByOrder(Long orderId);

    /**
     * Confirm a payment using Stripe payment intent
     * @param paymentIntentId Stripe payment intent ID
     * @return PaymentResponse with updated status
     */
    PaymentResponse confirmPayment(String paymentIntentId);

    /**
     * Refund a payment
     * @param paymentId Payment ID
     * @return PaymentResponse with refunded status
     */
    PaymentResponse refundPayment(Long paymentId);

    /**
     * Cancel a payment
     * @param paymentId Payment ID
     * @return PaymentResponse with cancelled status
     */
    PaymentResponse cancelPayment(Long paymentId);

    /**
     * Get payment status from Stripe
     * @param paymentIntentId Stripe payment intent ID
     * @return PaymentResponse with current status
     */
    PaymentResponse getPaymentStatus(String paymentIntentId);
}

