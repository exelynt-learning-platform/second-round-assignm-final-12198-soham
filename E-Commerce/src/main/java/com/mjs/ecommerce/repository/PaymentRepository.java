package com.mjs.ecommerce.repository;

import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    // Find payment by Stripe Payment Intent ID
    Optional<Payment> findByStripePaymentIntentId(String stripePaymentIntentId);

    // Find all payments for a specific user
    List<Payment> findByUserId(Long userId);

    // Find all payments for a specific order
    List<Payment> findByOrderId(Long orderId);

    // Find payment by order and status
    List<Payment> findByOrderIdAndStatus(Long orderId, PaymentStatus status);

    // Find all payments with specific status
    List<Payment> findByStatus(PaymentStatus status);

    // Find all payments for a user with specific status
    @Query("SELECT p FROM Payment p WHERE p.user.id = :userId AND p.status = :status")
    List<Payment> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") PaymentStatus status);
}

