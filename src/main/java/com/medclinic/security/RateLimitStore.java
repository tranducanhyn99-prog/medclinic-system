package com.medclinic.security;

public interface RateLimitStore {
    Decision increment(String bucketKey, long nowMillis, long windowMillis, int limit);

    record Decision(boolean allowed, long retryAfterSeconds) {
    }
}
