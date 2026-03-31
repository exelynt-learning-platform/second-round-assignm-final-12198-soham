package com.mjs.ecommerce.config;

import com.stripe.Stripe;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StripeConfig {

    private static final Logger logger = LoggerFactory.getLogger(StripeConfig.class);

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.publishable.key:}")
    private String stripePublishableKey;

    /**
     * Initialize and validate Stripe API keys after Spring context is fully loaded
     * Validates that keys are properly set before initializing Stripe
     */
    @PostConstruct
    public void initStripeApiKey() {
        // Check if Stripe keys are configured
        if (isStripeKeysMissing()) {
            logger.warn("⚠️ STRIPE CONFIGURATION WARNING:");
            logger.warn("Stripe API keys are not properly configured.");
            logger.warn("Payment functionality will be disabled.");
            logger.warn("To enable Stripe payments, set the following environment variables:");
            logger.warn("  - STRIPE_API_KEY: Your Stripe secret key (sk_test_... or sk_live_...)");
            logger.warn("  - STRIPE_PUBLISHABLE_KEY: Your Stripe publishable key (pk_test_... or pk_live_...)");
            logger.warn("Get your keys from: https://dashboard.stripe.com/apikeys");
            return;
        }

        // Validate Stripe secret key format
        if (!isValidStripeSecretKey(stripeApiKey)) {
            logger.error("❌ STRIPE CONFIGURATION ERROR:");
            logger.error("Invalid Stripe secret key format. Key must start with 'sk_test_' or 'sk_live_'");
            logger.error("Current key starts with: {}", stripeApiKey.substring(0, Math.min(10, stripeApiKey.length())));
            return;
        }

        // Validate Stripe publishable key format
        if (!isValidStripePublishableKey(stripePublishableKey)) {
            logger.error("❌ STRIPE CONFIGURATION ERROR:");
            logger.error("Invalid Stripe publishable key format. Key must start with 'pk_test_' or 'pk_live_'");
            logger.error("Current key starts with: {}", stripePublishableKey.substring(0, Math.min(10, stripePublishableKey.length())));
            return;
        }

        // Initialize Stripe with validated key
        try {
            Stripe.apiKey = stripeApiKey;
            logger.info("✅ Stripe API initialized successfully");
            logger.info("Environment: {}", stripeApiKey.contains("test") ? "TEST" : "PRODUCTION");
        } catch (Exception e) {
            logger.error("❌ Failed to initialize Stripe API: {}", e.getMessage());
        }
    }

    /**
     * Check if Stripe keys are missing or empty
     */
    private boolean isStripeKeysMissing() {
        return stripeApiKey == null || stripeApiKey.isEmpty() ||
               stripePublishableKey == null || stripePublishableKey.isEmpty();
    }

    /**
     * Validate Stripe secret key format
     * Must start with sk_test_ or sk_live_
     */
    private boolean isValidStripeSecretKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        return key.startsWith("sk_test_") || key.startsWith("sk_live_");
    }

    /**
     * Validate Stripe publishable key format
     * Must start with pk_test_ or pk_live_
     */
    private boolean isValidStripePublishableKey(String key) {
        if (key == null || key.isEmpty()) {
            return false;
        }
        return key.startsWith("pk_test_") || key.startsWith("pk_live_");
    }

    /**
     * Get Stripe API key status (for health checks)
     */
    public String getStripeKeyStatus() {
        if (isStripeKeysMissing()) {
            return "NOT_CONFIGURED";
        }
        if (!isValidStripeSecretKey(stripeApiKey) || !isValidStripePublishableKey(stripePublishableKey)) {
            return "INVALID_FORMAT";
        }
        return stripeApiKey.contains("test") ? "TEST_MODE" : "PRODUCTION_MODE";
    }
}



