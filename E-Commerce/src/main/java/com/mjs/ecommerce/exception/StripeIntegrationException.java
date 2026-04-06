package com.mjs.ecommerce.exception;


public class StripeIntegrationException extends RuntimeException {

    public StripeIntegrationException(String message) {
        super(message);
    }

    public StripeIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}