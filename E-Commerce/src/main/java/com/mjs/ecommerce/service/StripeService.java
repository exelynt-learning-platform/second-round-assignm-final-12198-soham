package com.mjs.ecommerce.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        if (stripeApiKey == null || stripeApiKey.isEmpty()) {
            throw new IllegalStateException("Stripe API key is missing!");
        }
        com.stripe.Stripe.apiKey = stripeApiKey;
    }

    public PaymentIntent createPaymentIntent(PaymentIntentCreateParams params) {
        try {
            return PaymentIntent.create(params);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe create failed: " + e.getMessage());
        }
    }

    public PaymentIntent retrieve(String id) {
        try {
            return PaymentIntent.retrieve(id);
        } catch (StripeException e) {
            throw new RuntimeException("Stripe retrieve failed: " + e.getMessage());
        }
    }

    public void cancel(String id) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(id);
            intent.cancel();
        } catch (StripeException e) {
            throw new RuntimeException("Stripe cancel failed: " + e.getMessage());
        }
    }
}