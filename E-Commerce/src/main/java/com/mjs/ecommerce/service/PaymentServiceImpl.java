package com.mjs.ecommerce.service;

import com.mjs.ecommerce.constants.Constants;
import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.dto.PaymentResponse;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.mapper.PaymentMapper;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.model.User;
import com.mjs.ecommerce.repository.OrderRepo;
import com.mjs.ecommerce.repository.PaymentRepository;
import com.mjs.ecommerce.repository.UserRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OrderRepo orderRepository;

    @Autowired
    private PaymentMapper paymentMapper;

    // =========================
    // CREATE PAYMENT
    // =========================
    @Override
    @Transactional
    public PaymentResponse createPayment(Long userId, PaymentRequest request) {

        log.info("Creating payment for userId={} orderId={}", userId, request.getOrderId());

        User user = getUser(userId);
        Order order = getOrder(request.getOrderId());

        validateOwnership(userId, order);
        validateAmount(request.getAmount());

        PaymentIntent intent = stripeService.createPaymentIntent(buildStripeParams(request));

        Payment payment = paymentMapper.createPayment(
                user, order, request, intent.getId()
        );

        paymentRepository.save(payment);

        log.info("Payment created successfully id={}", payment.getId());

        return buildResponse(payment, "Payment created");
    }

    // =========================
    // CONFIRM PAYMENT
    // =========================
    @Override
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            PaymentStatus status = mapStripeStatusToPaymentStatus(intent.getStatus());

            payment.setStatus(status);
            payment.setUpdatedAt(LocalDateTime.now());

            if ("succeeded".equals(intent.getStatus())) {
                payment.setFailureReason(null);
            } else if (intent.getLastPaymentError() != null) {
                payment.setFailureReason(intent.getLastPaymentError().getMessage());
            }

            paymentRepository.save(payment);

            return buildResponse(payment, "Payment status: " + intent.getStatus());

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    // =========================
    // REFUND PAYMENT
    // =========================
    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        try {
            Payment payment = getPaymentById(paymentId);

            if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
                throw new RuntimeException("Only successful payments can be refunded");
            }

            PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            intent.cancel();

            payment.setStatus(PaymentStatus.REFUNDED);
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            return buildResponse(payment, "Payment refunded successfully");

        } catch (StripeException e) {
            throw new RuntimeException("Stripe refund error: " + e.getMessage());
        }
    }

    // =========================
    // CANCEL PAYMENT
    // =========================
    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        try {
            Payment payment = getPaymentById(paymentId);

            if (payment.getStatus() == PaymentStatus.REFUNDED ||
                    payment.getStatus() == PaymentStatus.FAILED) {
                throw new RuntimeException("This payment cannot be cancelled");
            }

            PaymentIntent intent = PaymentIntent.retrieve(payment.getStripePaymentIntentId());
            intent.cancel();

            payment.setStatus(PaymentStatus.CANCELLED);
            payment.setUpdatedAt(LocalDateTime.now());

            paymentRepository.save(payment);

            return buildResponse(payment, "Payment cancelled successfully");

        } catch (StripeException e) {
            throw new RuntimeException("Stripe cancellation error: " + e.getMessage());
        }
    }

    // =========================
    // GET METHODS
    // =========================
    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Payment not found"));
    }

    @Override
    public List<Payment> getPaymentsByUser(Long userId) {
        getUser(userId);
        return paymentRepository.findByUserId(userId);
    }

    @Override
    public List<Payment> getPaymentsByOrder(Long orderId) {
        getOrder(orderId);
        return paymentRepository.findByOrderId(orderId);
    }

    @Override
    public PaymentResponse getPaymentStatus(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);

            Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                    .orElseThrow(() -> new RuntimeException("Payment not found"));

            return new PaymentResponse(
                    payment.getId(),
                    payment.getOrder().getId(),
                    payment.getAmount(),
                    payment.getCurrency(),
                    mapStripeStatusToPaymentStatus(intent.getStatus()),
                    payment.getPaymentMethod(),
                    payment.getStripePaymentIntentId()
            );

        } catch (StripeException e) {
            throw new RuntimeException("Stripe error: " + e.getMessage());
        }
    }

    // =========================
    // PRIVATE HELPERS
    // =========================

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(Constants.USER_NOT_FOUND));
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException(Constants.ORDER_NOT_FOUND));
    }

    private void validateOwnership(Long userId, Order order) {
        if (!order.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Order does not belong to user");
        }
    }

    private void validateAmount(Double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private PaymentIntentCreateParams buildStripeParams(PaymentRequest request) {
        return PaymentIntentCreateParams.builder()
                .setAmount(Math.round(request.getAmount() * 100))
                .setCurrency(request.getCurrency().toLowerCase())
                .setDescription(request.getDescription())
                .putMetadata("orderId", request.getOrderId().toString())
                .build();
    }

    private PaymentResponse buildResponse(Payment payment, String message) {
        PaymentResponse response = new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getStripePaymentIntentId()
        );
        response.setMessage(message);
        return response;
    }

    private PaymentStatus mapStripeStatusToPaymentStatus(String status) {
        return switch (status) {
            case "succeeded" -> PaymentStatus.SUCCESSFUL;
            case "processing", "requires_payment_method",
                 "requires_confirmation", "requires_action" -> PaymentStatus.PENDING;
            case "canceled" -> PaymentStatus.CANCELLED;
            case "refunded" -> PaymentStatus.REFUNDED;
            case "paid" -> PaymentStatus.PAID;
            default -> PaymentStatus.FAILED;

        };
    }
}