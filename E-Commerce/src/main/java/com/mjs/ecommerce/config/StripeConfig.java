package com.mjs.ecommerce.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    /**
     * Initialize Stripe API key after Spring context is fully loaded
     * Uses PostConstruct to avoid issues during application startup
     */
    @PostConstruct
    public void initStripeApiKey() {
        if (stripeApiKey != null && !stripeApiKey.isEmpty() && !stripeApiKey.contains("YOUR")) {
            Stripe.apiKey = stripeApiKey;
        }
    }
}


