package com.example.common.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in‑memory rate‑limit filter using a sliding‑window counter.
 * Allows up to {@code MAX_REQUESTS_PER_MINUTE} requests per IP address per minute.
 * This implementation avoids external dependencies (e.g., Bucket4j) for simplicity.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    // Stores counters per IP address and the timestamp of the window start (epoch seconds).
    private final Map<String, Counter> counters = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String clientIp = getClientIp(request);
        long now = Instant.now().getEpochSecond();
        Counter counter = counters.computeIfAbsent(clientIp, ip -> new Counter(now));

        synchronized (counter) {
            // Reset window if more than a minute has passed.
            if (now - counter.windowStart >= 60) {
                counter.windowStart = now;
                counter.requestCount.set(0);
            }
            // Increment and check limit.
            int current = counter.requestCount.incrementAndGet();
            if (current > MAX_REQUESTS_PER_MINUTE) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.getWriter().write("Rate limit exceeded");
                return;
            }
        }
        // Proceed with the chain if under limit.
        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0].trim();
    }

    /** Helper holder for per‑IP counters. */
    private static class Counter {
        volatile long windowStart;
        final AtomicInteger requestCount = new AtomicInteger();
        Counter(long start) {
            this.windowStart = start;
        }
    }
}
