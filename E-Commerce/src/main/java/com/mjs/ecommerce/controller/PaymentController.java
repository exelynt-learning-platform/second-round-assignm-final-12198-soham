package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.service.PaymentServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentServiceImpl paymentService;

    /**
     * Create a new payment using Stripe
     * POST /api/payments/create/{userId}
     */
    @PostMapping("/create/{userId}")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long userId,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            PaymentResponse response = paymentService.createPayment(userId, paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Payment creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Confirm a payment by Stripe Payment Intent ID
     * POST /api/payments/confirm/{paymentIntentId}
     */
    @PostMapping("/confirm/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentResponse response = paymentService.confirmPayment(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Payment confirmation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Get payment by ID
     * GET /api/payments/{paymentId}
     */
    @GetMapping("/{paymentId}")
    public ResponseEntity<Payment> getPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentService.getPaymentById(paymentId);
            return ResponseEntity.ok(payment);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Get all payments for a user
     * GET /api/payments/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Payment>> getPaymentsByUser(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByUser(userId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Get all payments for an order
     * GET /api/payments/order/{orderId}
     */
    @GetMapping("/order/{orderId}")
    public ResponseEntity<List<Payment>> getPaymentsByOrder(@PathVariable Long orderId) {
        try {
            List<Payment> payments = paymentService.getPaymentsByOrder(orderId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Get payment status
     * GET /api/payments/status/{paymentIntentId}
     */
    @GetMapping("/status/{paymentIntentId}")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            PaymentResponse response = paymentService.getPaymentStatus(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Failed to retrieve payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    /**
     * Refund a payment
     * POST /api/payments/refund/{paymentId}
     */
    @PostMapping("/refund/{paymentId}")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponse response = paymentService.refundPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Refund failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    /**
     * Cancel a payment
     * POST /api/payments/cancel/{paymentId}
     */
    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponse response = paymentService.cancelPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Cancellation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}

