package com.mjs.ecommerce.service;

import com.mjs.ecommerce.exception.StripeIntegrationException;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@Slf4j
public class StripeService {

    @Value("${stripe.api.key}")
    private  String stripeApiKey;

    @PostConstruct
    public void init() {
        try {
            if (!StringUtils.hasText(stripeApiKey)) {
                log.error("Stripe initialization failed: API key is missing");
                throw new StripeIntegrationException(
                        "Stripe API key is missing. Set STRIPE_API_KEY environment variable."
                );
            }

            Stripe.apiKey = stripeApiKey;
            log.info("Stripe initialized successfully");
        } catch (Exception ex) {
            log.error("Stripe initialization failed", ex);
            throw new StripeIntegrationException(
                    "Unable to initialize Stripe. Check configuration and API key.",
                    ex
            );
        }
    }

    public PaymentIntent createPaymentIntent(PaymentIntentCreateParams params) {
        try {
            return PaymentIntent.create(params);
        } catch (StripeException ex) {
            log.error("Stripe createPaymentIntent failed", ex);
            throw new StripeIntegrationException(
                    "Stripe create payment failed: " + ex.getMessage(),
                    ex
            );
        }
    }

    public PaymentIntent retrieve(String paymentIntentId) {
        try {
            return PaymentIntent.retrieve(paymentIntentId);
        } catch (StripeException ex) {
            log.error("Stripe retrieve failed for paymentIntentId={}", paymentIntentId, ex);
            throw new StripeIntegrationException(
                    "Stripe retrieve failed: " + ex.getMessage(),
                    ex
            );
        }
    }

    public void cancel(String paymentIntentId) {
        try {
            PaymentIntent intent = PaymentIntent.retrieve(paymentIntentId);
            intent.cancel();
        } catch (StripeException ex) {
            log.error("Stripe cancel failed for paymentIntentId={}", paymentIntentId, ex);
            throw new StripeIntegrationException(
                    "Stripe cancel failed: " + ex.getMessage(),
                    ex
            );
        }
    }
}