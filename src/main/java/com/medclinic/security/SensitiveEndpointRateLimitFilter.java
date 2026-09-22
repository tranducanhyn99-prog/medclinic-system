package com.medclinic.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.LongSupplier;

@Component
public class SensitiveEndpointRateLimitFilter extends OncePerRequestFilter {
    private final int loginMaxRequests;
    private final int passwordResetMaxRequests;
    private final long windowMillis;
    private final SecurityAuditService auditService;
    private final LongSupplier clock;
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    @Autowired
    public SensitiveEndpointRateLimitFilter(
            @Value("${app.security.rate-limit.login-max-requests:20}") int loginMaxRequests,
            @Value("${app.security.rate-limit.password-reset-max-requests:5}") int passwordResetMaxRequests,
            @Value("${app.security.rate-limit.window-seconds:300}") long windowSeconds,
            SecurityAuditService auditService) {
        this(loginMaxRequests, passwordResetMaxRequests, windowSeconds, auditService,
                System::currentTimeMillis);
    }

    SensitiveEndpointRateLimitFilter(
            int loginMaxRequests,
            int passwordResetMaxRequests,
            long windowSeconds,
            SecurityAuditService auditService,
            LongSupplier clock) {
        this.loginMaxRequests = loginMaxRequests;
        this.passwordResetMaxRequests = passwordResetMaxRequests;
        this.windowMillis = windowSeconds * 1000L;
        this.auditService = auditService;
        this.clock = clock;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String endpoint = protectedEndpoint(request);
        if (endpoint == null) {
            filterChain.doFilter(request, response);
            return;
        }
        long now = clock.getAsLong();
        String remoteIp = request.getRemoteAddr();
        String key = remoteIp + '|' + endpoint;
        int limit = endpoint.equals("/auth/login") ? loginMaxRequests : passwordResetMaxRequests;

        WindowCounter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.windowStartMillis() >= windowMillis) {
                return new WindowCounter(now, 1);
            }
            return new WindowCounter(current.windowStartMillis(), current.count() + 1);
        });

        if (counters.size() > 1000) {
            counters.entrySet().removeIf(entry ->
                    now - entry.getValue().windowStartMillis() >= windowMillis);
        }

        if (counter.count() > limit) {
            long retryAfterSeconds = Math.max(1L,
                    (counter.windowStartMillis() + windowMillis - now + 999L) / 1000L);
            auditService.rateLimitRejected(endpoint, remoteIp);
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Too many requests. Please try again later.");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String protectedEndpoint(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return null;
        }
        String path = request.getRequestURI().substring(request.getContextPath().length());
        if ("/auth/login".equals(path)
                || "/auth/otp".equals(path)
                || "/auth/otp/resend".equals(path)
                || "/forgot-password".equals(path)
                || "/reset-password".equals(path)) {
            return path;
        }
        return null;
    }

    private record WindowCounter(long windowStartMillis, int count) {
    }
}
