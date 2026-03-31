package com.mjs.ecommerce.service;

import com.mjs.ecommerce.Constants;
import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.PaymentRepository;
import com.mjs.ecommerce.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepo orderRepository;

    @Override
    @Transactional
    public PaymentResponse createPayment(Long userId, PaymentRequest paymentRequest) {
        try {
            // Validate user exists
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));

            // Validate order exists
            Order order = orderRepository.findById(paymentRequest.getOrderId())
                    .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));

            if (order.getUser().getId()!=userId) {
                throw new IllegalArgumentException("Cannot pay for an order that does not belong to you. Order belongs to user ID: " + order.getUser().getId());
            }

            if (paymentRequest.getAmount() <= 0) {
                throw new IllegalArgumentException("Payment amount must be greater than zero");
            }

            long amountInCents = Math.round(paymentRequest.getAmount() * 100);

            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(paymentRequest.getCurrency().toLowerCase())
                    .setDescription(paymentRequest.getDescription())
                    .putMetadata("orderId", paymentRequest.getOrderId().toString())
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            // Save payment to database
            Payment payment = new Payment(
                    order,
                    user,
                    paymentIntent.getId(),
                    paymentRequest.getAmount(),
                    paymentRequest.getCurrency(),
                    mapStripeStatusToPaymentStatus(paymentIntent.getStatus()),
                    paymentRequest.getPaymentMethodToken()
            );
            payment.setDescription(paymentRequest.getDescription());

            paymentRepository.save(payment);

            // Build response
            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );
            response.setMessage("Payment intent created successfully. Amount: " + paymentRequest.getAmount() + " " + paymentRequest.getCurrency());

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Payment creation failed: " + e.getMessage());
        }
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> getPaymentsByOrder(Long orderId) {
        orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(Constants.PRODUCT_NOT_FOUND));
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId) {
        try {
            // Fetch payment intent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Find payment in database
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            // Update payment status
            PaymentStatus newStatus = mapStripeStatusToPaymentStatus(paymentIntent.getStatus());
            payment.setStatus(newStatus);
            payment.setUpdatedAt(LocalDateTime.now());

            // Check for failure
            if ("succeeded".equals(paymentIntent.getStatus())) {
                payment.setFailureReason(null);
            } else if (paymentIntent.getLastPaymentError() != null) {
                payment.setFailureReason(paymentIntent.getLastPaymentError().getMessage());
            }

            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );

            if ("succeeded".equals(paymentIntent.getStatus())) {
                response.setMessage("Payment confirmed successfully");
            } else {
                response.setMessage("Payment status: " + paymentIntent.getStatus());
                response.setFailureReason(payment.getFailureReason());
            }

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        try {
            Payment payment = getPaymentById(paymentId);

            // Check if payment is successful before refunding
            if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
                throw new RuntimeException("Only successful payments can be refunded");
            }

            // Create refund using Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            paymentIntent.cancel();

            // Update payment status
            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );
            response.setMessage("Payment refunded successfully");

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe refund error: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        try {
            Payment payment = getPaymentById(paymentId);

            // Check if payment can be cancelled
            if (payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.FAILED) {
                throw new RuntimeException("This payment cannot be cancelled");
            }

            // Cancel payment intent in Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            paymentIntent.cancel();

            // Update payment status
            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setUpdatedAt(LocalDateTime.now());
            paymentRepository.save(payment);

            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    payment.getStatus(),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );
            response.setMessage("Payment cancelled successfully");

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe cancellation error: " + e.getMessage());
        }
    }

    @Override
    public PaymentResponse getPaymentStatus(String paymentIntentId) {
        try {
            // Fetch payment intent from Stripe
            PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

            // Find payment in database
            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            PaymentResponse response = new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    mapStripeStatusToPaymentStatus(paymentIntent.getStatus()),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );
            response.setMessage("Current status: " + paymentIntent.getStatus());

            return response;

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    /**
     * Map Stripe payment status to application PaymentStatus
     */
    private PaymentStatus mapStripeStatusToPaymentStatus(String stripeStatus) {
        switch (stripeStatus) {
            case "succeeded":
                return PaymentStatus.SUCCESSFUL;
            case "processing":
                return PaymentStatus.PENDING;
            case "requires_payment_method":
            case "requires_confirmation":
            case "requires_action":
                return PaymentStatus.PENDING;
            case "canceled":
                return PaymentStatus.CANCELLED;
            default:
                return PaymentStatus.FAILED;
        }
    }
}

