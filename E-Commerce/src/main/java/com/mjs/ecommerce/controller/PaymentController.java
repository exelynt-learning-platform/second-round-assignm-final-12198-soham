package com.mjs.ecommerce.controller;

import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.service.PaymentServiceImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentServiceImpl paymentServiceImpl;

    @PostMapping("/create/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> createPayment(
            @PathVariable Long userId,
            @Valid @RequestBody PaymentRequest paymentRequest) {
        try {
            PaymentResponse response = paymentServiceImpl.createPayment(userId, paymentRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Payment creation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/confirm/{paymentIntentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> confirmPayment(@PathVariable String paymentIntentId) {
        try {
            PaymentResponse response = paymentServiceImpl.confirmPayment(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Payment confirmation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    // ✅ FIXED: Returns proper error response instead of null
    @GetMapping("/{paymentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        try {
            Payment payment = paymentServiceImpl.getPaymentById(paymentId);
            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Payment not found: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // ✅ FIXED: Returns proper error response instead of null
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPaymentsByUser(@PathVariable Long userId) {
        try {
            List<Payment> payments = paymentServiceImpl.getPaymentsByUser(userId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Failed to retrieve payments for user: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    // ✅ FIXED: Returns proper error response instead of null
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getPaymentsByOrder(@PathVariable Long orderId) {
        try {
            List<Payment> payments = paymentServiceImpl.getPaymentsByOrder(orderId);
            return ResponseEntity.ok(payments);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Failed to retrieve payments for order: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @GetMapping("/status/{paymentIntentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> getPaymentStatus(@PathVariable String paymentIntentId) {
        try {
            PaymentResponse response = paymentServiceImpl.getPaymentStatus(paymentIntentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Failed to retrieve payment status: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
        }
    }

    @PostMapping("/refund/{paymentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> refundPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponse response = paymentServiceImpl.refundPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Refund failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }

    @PostMapping("/cancel/{paymentId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PaymentResponse> cancelPayment(@PathVariable Long paymentId) {
        try {
            PaymentResponse response = paymentServiceImpl.cancelPayment(paymentId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            PaymentResponse errorResponse = new PaymentResponse();
            errorResponse.setMessage("Cancellation failed: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        }
    }
}