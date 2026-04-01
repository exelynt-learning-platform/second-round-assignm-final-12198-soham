package com.mjs.ecommerce.service;


import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RateLimiterServiceTest {

    private RateLimiterService rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new RateLimiterService();
    }

    // =========================
    // ALLOW REQUEST SUCCESS
    // =========================
    @Test
    void allowRequest_shouldAllowWithinLimit() {

        String key = "test-ip";

        boolean result = rateLimiterService.allowRequest(key);

        assertTrue(result);
    }

    // =========================
    // RATE LIMIT EXCEEDED
    // =========================
    @Test
    void allowRequest_shouldBlockAfterLimitExceeded() {

        String key = "test-ip";

        // consume all 5 tokens
        for (int i = 0; i < 5; i++) {
            assertTrue(rateLimiterService.allowRequest(key));
        }

        // 6th request should fail
        boolean result = rateLimiterService.allowRequest(key);

        assertFalse(result);
    }

    // =========================
    // REMAINING REQUESTS
    // =========================
    @Test
    void getRemainingRequests_shouldDecreaseAfterConsumption() {

        String key = "test-ip";

        long initial = rateLimiterService.getRemainingRequests(key);

        rateLimiterService.allowRequest(key);

        long after = rateLimiterService.getRemainingRequests(key);

        assertEquals(initial - 1, after);
    }

    // =========================
    // RESET LIMIT
    // =========================
    @Test
    void resetLimit_shouldRestoreTokens() {

        String key = "test-ip";

        // exhaust tokens
        for (int i = 0; i < 5; i++) {
            rateLimiterService.allowRequest(key);
        }

        assertFalse(rateLimiterService.allowRequest(key)); // blocked

        // reset
        rateLimiterService.resetLimit(key);

        // should allow again
        assertTrue(rateLimiterService.allowRequest(key));
    }

    // =========================
    // CLEAR ALL LIMITS
    // =========================
    @Test
    void clearAllLimits_shouldResetAllKeys() {

        String key1 = "ip1";
        String key2 = "ip2";

        // consume tokens
        for (int i = 0; i < 5; i++) {
            rateLimiterService.allowRequest(key1);
            rateLimiterService.allowRequest(key2);
        }

        // both blocked
        assertFalse(rateLimiterService.allowRequest(key1));
        assertFalse(rateLimiterService.allowRequest(key2));

        // clear all
        rateLimiterService.clearAllLimits();

        // both should work again
        assertTrue(rateLimiterService.allowRequest(key1));
        assertTrue(rateLimiterService.allowRequest(key2));
    }

    // =========================
    // RESOLVE BUCKET SAME INSTANCE
    // =========================
    @Test
    void resolveBucket_shouldReturnSameBucketForSameKey() {

        String key = "test-ip";

        Bucket bucket1 = rateLimiterService.resolveBucket(key);
        Bucket bucket2 = rateLimiterService.resolveBucket(key);

        assertSame(bucket1, bucket2);
    }

    // =========================
    // STRICT LOGIN BUCKET
    // =========================
    @Test
    void createStrictLoginBucket_shouldLimitToThreeRequests() {

        Bucket bucket = rateLimiterService.createStrictLoginBucket();

        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));
        assertTrue(bucket.tryConsume(1));

        // 4th should fail
        assertFalse(bucket.tryConsume(1));
    }
}
