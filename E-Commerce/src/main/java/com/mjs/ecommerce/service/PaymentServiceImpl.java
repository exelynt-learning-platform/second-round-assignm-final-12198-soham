package com.mjs.ecommerce.service;

import com.mjs.ecommerce.exception.InvalidAmountException;
import com.mjs.ecommerce.exception.OrderNotFoundException;
import com.mjs.ecommerce.exception.PaymentNotFoundException;
import com.mjs.ecommerce.exception.PaymentOperationException;
import com.mjs.ecommerce.exception.UnauthorizedOrderAccessException;
import com.mjs.ecommerce.exception.UserNotFoundException;
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

    private StripeService stripeService;

    private PaymentRepository paymentRepository;

    private UserRepository userRepository;

    private OrderRepo orderRepository;

    private PaymentMapper paymentMapper;

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

    @Override
    @Transactional
    public PaymentResponse confirmPayment(String paymentIntentId) {
        PaymentIntent intent = stripeService.retrieve(paymentIntentId);

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(PaymentNotFoundException::new);

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
    }

    @Override
    @Transactional
    public PaymentResponse refundPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() != PaymentStatus.SUCCESSFUL) {
            throw new PaymentOperationException("Only successful payments can be refunded");
        }

        stripeService.cancel(payment.getStripePaymentIntentId());

        payment.setStatus(PaymentStatus.REFUNDED);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return buildResponse(payment, "Payment refunded successfully");
    }

    @Override
    @Transactional
    public PaymentResponse cancelPayment(Long paymentId) {
        Payment payment = getPaymentById(paymentId);

        if (payment.getStatus() == PaymentStatus.REFUNDED ||
                payment.getStatus() == PaymentStatus.FAILED) {
            throw new PaymentOperationException("This payment cannot be cancelled");
        }

        stripeService.cancel(payment.getStripePaymentIntentId());

        payment.setStatus(PaymentStatus.CANCELLED);
        payment.setUpdatedAt(LocalDateTime.now());

        paymentRepository.save(payment);

        return buildResponse(payment, "Payment cancelled successfully");
    }

    @Override
    public Payment getPaymentById(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(PaymentNotFoundException::new);
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
        PaymentIntent intent = stripeService.retrieve(paymentIntentId);

        Payment payment = paymentRepository.findByStripePaymentIntentId(paymentIntentId)
                .orElseThrow(PaymentNotFoundException::new);

        return new PaymentResponse(
                payment.getId(),
                payment.getOrder().getId(),
                payment.getAmount(),
                payment.getCurrency(),
                mapStripeStatusToPaymentStatus(intent.getStatus()),
                payment.getPaymentMethod(),
                payment.getStripePaymentIntentId()
        );
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(OrderNotFoundException::new);
    }

    private void validateOwnership(Long userId, Order order) {
        if (!order.getUser().getId().equals(userId)) {
            throw new UnauthorizedOrderAccessException();
        }
    }

    private void validateAmount(Double amount) {
        if (amount <= 0) {
            throw new InvalidAmountException();
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