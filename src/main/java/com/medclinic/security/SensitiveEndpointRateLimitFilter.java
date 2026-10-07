package com.medclinic.security;

import com.medclinic.util.Sha256;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.function.LongSupplier;

@Component
public class SensitiveEndpointRateLimitFilter extends OncePerRequestFilter {
    private final int loginMaxRequests;
    private final int passwordResetMaxRequests;
    private final int paymentCallbackMaxRequests;
    private final long windowMillis;
    private final SecurityAuditService auditService;
    private final RateLimitStore rateLimitStore;
    private final LongSupplier clock;

    @Autowired
    public SensitiveEndpointRateLimitFilter(
            @Value("${app.security.rate-limit.login-max-requests:20}") int loginMaxRequests,
            @Value("${app.security.rate-limit.password-reset-max-requests:5}") int passwordResetMaxRequests,
            @Value("${app.security.rate-limit.payment-callback-max-requests:60}") int paymentCallbackMaxRequests,
            @Value("${app.security.rate-limit.window-seconds:300}") long windowSeconds,
            SecurityAuditService auditService,
            RateLimitStore rateLimitStore) {
        this(loginMaxRequests, passwordResetMaxRequests, paymentCallbackMaxRequests,
                windowSeconds, auditService, rateLimitStore,
                System::currentTimeMillis);
    }

    SensitiveEndpointRateLimitFilter(
            int loginMaxRequests,
            int passwordResetMaxRequests,
            long windowSeconds,
            SecurityAuditService auditService,
            LongSupplier clock) {
        this(loginMaxRequests, passwordResetMaxRequests, 60, windowSeconds, auditService,
                new InMemoryRateLimitStore(), clock);
    }

    SensitiveEndpointRateLimitFilter(
            int loginMaxRequests,
            int passwordResetMaxRequests,
            int paymentCallbackMaxRequests,
            long windowSeconds,
            SecurityAuditService auditService,
            RateLimitStore rateLimitStore,
            LongSupplier clock) {
        this.loginMaxRequests = loginMaxRequests;
        this.passwordResetMaxRequests = passwordResetMaxRequests;
        this.paymentCallbackMaxRequests = paymentCallbackMaxRequests;
        this.windowMillis = windowSeconds * 1000L;
        this.auditService = auditService;
        this.rateLimitStore = rateLimitStore;
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
        String key = Sha256.hex(remoteIp + '|' + endpoint);
        int limit = switch (endpoint) {
            case "/auth/login" -> loginMaxRequests;
            case "/api/payments/bank/callback" -> paymentCallbackMaxRequests;
            default -> passwordResetMaxRequests;
        };
        RateLimitStore.Decision decision = rateLimitStore.increment(key, now, windowMillis, limit);

        if (!decision.allowed()) {
            auditService.rateLimitRejected(endpoint, remoteIp);
            response.setStatus(429);
            response.setHeader("Retry-After", Long.toString(decision.retryAfterSeconds()));
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
                || "/reset-password".equals(path)
                || "/api/payments/bank/callback".equals(path)) {
            return path;
        }
        return null;
    }

}
