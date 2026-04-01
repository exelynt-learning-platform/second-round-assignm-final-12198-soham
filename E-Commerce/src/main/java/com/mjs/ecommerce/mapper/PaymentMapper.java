package com.mjs.ecommerce.mapper;

import com.mjs.ecommerce.dto.PaymentRequest;
import com.mjs.ecommerce.enums.PaymentStatus;
import com.mjs.ecommerce.model.Order;
import com.mjs.ecommerce.model.Payment;
import com.mjs.ecommerce.model.User;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
@Service
public class PaymentMapper {
    public Payment createPayment(User user,
                                 Order order,
                                 PaymentRequest request,
                                 String stripePaymentIntentId) {

        Payment payment = new Payment();

        payment.setUser(user);
        payment.setOrder(order);
        payment.setAmount(request.getAmount());
        payment.setCurrency(request.getCurrency());
        payment.setStatus(PaymentStatus.PENDING);
        payment.setPaymentMethod(request.getPaymentMethodToken());
        payment.setStripePaymentIntentId(stripePaymentIntentId);
        payment.setCreatedAt(LocalDateTime.now());
        payment.setUpdatedAt(LocalDateTime.now());
        payment.setDescription(request.getDescription());

        return payment;
    }
}
