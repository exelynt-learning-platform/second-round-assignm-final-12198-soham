package com.mjs.ecommerce.exception;

public class StripeCreatePaymentFailedException extends RuntimeException {
    public StripeCreatePaymentFailedException(String message) {
            super(message);
    }
}
