package com.mjs.ecommerce.exception;

public class PaymentOperationException extends RuntimeException {

    public PaymentOperationException() {
        super("Payment operation failed");
    }

    public PaymentOperationException(String message) {
        super(message);
    }

    public PaymentOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}