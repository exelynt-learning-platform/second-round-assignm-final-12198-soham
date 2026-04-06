package com.mjs.ecommerce.exception;

public class UnauthorizedOrderAccessException extends RuntimeException {

    public UnauthorizedOrderAccessException() {
        super("Order does not belong to user");
    }

    public UnauthorizedOrderAccessException(String message) {
        super(message);
    }
}