package com.slior.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slior.dto.ErrorResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int TOO_MANY_REQUESTS = 429;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Duration STATE_TTL = Duration.ofHours(2);
    private static final long CLEANUP_EVERY_REQUESTS = 250;
    private static final String MDC_KEY = "requestId";

    private final ConcurrentHashMap<String, BucketState> buckets = new ConcurrentHashMap<>();
    private final AtomicLong requestCounter = new AtomicLong();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (shouldSkip(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        RateLimitRule rule = resolveRule(request);
        String clientKey = resolveClientKey(request);
        String bucketKey = clientKey + "|" + rule.key();

        BucketState state = buckets.compute(bucketKey, (key, existing) -> {
            if (existing == null) {
                return new BucketState(createBucket(rule), System.currentTimeMillis());
            }
            existing.lastSeenAtMs = System.currentTimeMillis();
            return existing;
        });

        maybeCleanup();

        ConsumptionProbe probe = state.bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long waitNanos = probe.getNanosToWaitForRefill();
        long retryAfterSeconds = Math.max(1L, TimeUnit.NANOSECONDS.toSeconds(waitNanos));

        response.setStatus(TOO_MANY_REQUESTS);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

        ErrorResponse errorResponse = ErrorResponse.of(
                TOO_MANY_REQUESTS,
                "TOO_MANY_REQUESTS",
                "Demasiadas solicitudes. Inténtalo más tarde.",
                request.getRequestURI(),
                MDC.get(MDC_KEY)
        );

        response.getWriter().write(OBJECT_MAPPER.writeValueAsString(errorResponse));
    }

    private boolean shouldSkip(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private RateLimitRule resolveRule(HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        if ("POST".equalsIgnoreCase(method) && "/auth/v1/login".equals(path)) {
            return RateLimitRule.login();
        }
        if ("POST".equalsIgnoreCase(method) && "/auth/v1/register".equals(path)) {
            return RateLimitRule.register();
        }
        if ("GET".equalsIgnoreCase(method) && "/api/v1/geocode/search".equals(path)) {
            return RateLimitRule.geocode();
        }
        return RateLimitRule.defaultRule();
    }

    private Bucket createBucket(RateLimitRule rule) {
        Bandwidth limit = Bandwidth.classic(
                rule.capacity(),
                Refill.intervally(rule.capacity(), rule.window())
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientKey(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }

    private void maybeCleanup() {
        long current = requestCounter.incrementAndGet();
        if (current % CLEANUP_EVERY_REQUESTS != 0) {
            return;
        }

        long cutoff = System.currentTimeMillis() - STATE_TTL.toMillis();
        buckets.entrySet().removeIf(entry -> entry.getValue().lastSeenAtMs < cutoff);
    }

    private record RateLimitRule(String key, int capacity, Duration window) {
        static RateLimitRule login() {
            return new RateLimitRule("auth-login", 5, WINDOW);
        }

        static RateLimitRule register() {
            return new RateLimitRule("auth-register", 3, WINDOW);
        }

        static RateLimitRule geocode() {
            return new RateLimitRule("geocode-search", 30, WINDOW);
        }

        static RateLimitRule defaultRule() {
            return new RateLimitRule("default", 100, WINDOW);
        }
    }

    private static final class BucketState {
        private final Bucket bucket;
        private volatile long lastSeenAtMs;

        private BucketState(Bucket bucket, long lastSeenAtMs) {
            this.bucket = bucket;
            this.lastSeenAtMs = lastSeenAtMs;
        }
    }
}

