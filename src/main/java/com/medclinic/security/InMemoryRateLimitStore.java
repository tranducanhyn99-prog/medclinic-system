package com.medclinic.security;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@ConditionalOnProperty(name = "app.security.rate-limit.store", havingValue = "memory", matchIfMissing = true)
public class InMemoryRateLimitStore implements RateLimitStore {
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Override
    public Decision increment(String bucketKey, long nowMillis, long windowMillis, int limit) {
        WindowCounter counter = counters.compute(bucketKey, (ignored, current) -> {
            if (current == null || nowMillis - current.windowStartMillis() >= windowMillis) {
                return new WindowCounter(nowMillis, 1);
            }
            return new WindowCounter(current.windowStartMillis(), current.count() + 1);
        });
        if (counters.size() > 1000) {
            counters.entrySet().removeIf(entry ->
                    nowMillis - entry.getValue().windowStartMillis() >= windowMillis);
        }
        long retry = Math.max(1L,
                (counter.windowStartMillis() + windowMillis - nowMillis + 999L) / 1000L);
        return new Decision(counter.count() <= limit, retry);
    }

    private record WindowCounter(long windowStartMillis, int count) {
    }
}
