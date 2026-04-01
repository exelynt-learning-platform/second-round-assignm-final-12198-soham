package com.mjs.ecommerce.Exception;


public class RateLimitExceededException extends RuntimeException {

    public RateLimitExceededException() {
        super("Too many requests. Please try again later.");
    }

    public RateLimitExceededException(String message) {
        super(message);
    }
}