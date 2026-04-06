package com.mjs.ecommerce.exception;

public class CartNotFoundException extends RuntimeException {

    public CartNotFoundException() {
        super("Cart not found");
    }

    public CartNotFoundException(String message) {
        super(message);
    }
}