package com.mjs.ecommerce.exception;

public class InvalidQuantityException extends RuntimeException {

    public InvalidQuantityException() {
        super("Quantity must be greater than zero");
    }

    public InvalidQuantityException(String message) {
        super(message);
    }
}