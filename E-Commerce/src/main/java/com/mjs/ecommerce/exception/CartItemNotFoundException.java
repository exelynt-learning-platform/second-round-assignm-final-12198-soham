package com.mjs.ecommerce.exception;

public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException() {
        super("Cart items are null or empty");
    }

    public CartItemNotFoundException(String message) {
        super(message);
    }
}