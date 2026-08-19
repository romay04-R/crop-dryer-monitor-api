package com.example.cropdryer.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitConfig {

    @Value("${cropdryer.rate-limit.enabled:true}")
    private boolean rateLimitEnabled;

    @Value("${cropdryer.rate-limit.capacity:100}")
    private int capacity;

    @Value("${cropdryer.rate-limit.refill-tokens:10}")
    private int refillTokens;

    @Value("${cropdryer.rate-limit.refill-duration:1}")
    private int refillDurationSeconds;

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    public Bucket resolveBucket(String key) {
        if (!rateLimitEnabled) {
            return Bucket.builder()
                    .addLimit(Bandwidth.classic(Integer.MAX_VALUE, Refill.intervally(Integer.MAX_VALUE, Duration.ofSeconds(1))))
                    .build();
        }

        return cache.computeIfAbsent(key, k -> {
            Bandwidth limit = Bandwidth.classic(capacity, Refill.greedy(refillTokens, Duration.ofSeconds(refillDurationSeconds)));
            return Bucket.builder()
                    .addLimit(limit)
                    .build();
        });
    }

    public boolean isRateLimitEnabled() {
        return rateLimitEnabled;
    }
}
