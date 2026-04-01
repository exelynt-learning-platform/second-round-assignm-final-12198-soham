package com.mjs.ecommerce.service;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class RateLimiterService {

    // Store buckets per identifier (IP address or email)
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    /**
     * Creates or retrieves a bucket for rate limiting
     * Default: 5 requests per 15 minutes
     */
    public Bucket resolveBucket(String key) {
        return cache.computeIfAbsent(key, k -> createNewBucket());
    }

    /**
     * Checks if a request is allowed within rate limit
     * @param key unique identifier (IP address, email, etc.)
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String key) {
        Bucket bucket = resolveBucket(key);
        return bucket.tryConsume(1);
    }

    /**
     * Gets remaining requests for a key
     */
    public long getRemainingRequests(String key) {
        return resolveBucket(key).getAvailableTokens();
    }

    /**
     * Creates a new bucket with rate limit configuration
     * Limits: 5 requests per 15 minutes
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(5, Refill.intervally(5, Duration.ofMinutes(15)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Alternative bucket configuration for stricter login rate limiting
     * Limits: 3 requests per 15 minutes
     */
    public Bucket createStrictLoginBucket() {
        Bandwidth limit = Bandwidth.classic(3, Refill.intervally(3, Duration.ofMinutes(15)));
        return Bucket4j.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Reset rate limit for a specific key (admin use)
     */
    public void resetLimit(String key) {
        cache.remove(key);
    }

    /**
     * Clear all rate limits (use cautiously)
     */
    public void clearAllLimits() {
        cache.clear();
    }
}