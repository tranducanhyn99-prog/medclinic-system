package com.medclinic.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SensitiveEndpointRateLimitFilterTest {

    @Test
    void rejectsRequestsAboveLimitAndAllowsNextWindow() throws Exception {
        AtomicLong clock = new AtomicLong(1_000L);
        SecurityAuditService audit = mock(SecurityAuditService.class);
        SensitiveEndpointRateLimitFilter filter =
                new SensitiveEndpointRateLimitFilter(2, 1, 60, audit, clock::get);

        assertEquals(200, execute(filter, "POST", "/auth/login", "10.0.0.1").getStatus());
        assertEquals(200, execute(filter, "POST", "/auth/login", "10.0.0.1").getStatus());

        MockHttpServletResponse blocked = execute(filter, "POST", "/auth/login", "10.0.0.1");
        assertEquals(429, blocked.getStatus());
        assertEquals("60", blocked.getHeader("Retry-After"));
        verify(audit).rateLimitRejected("/auth/login", "10.0.0.1");

        clock.set(61_001L);
        assertEquals(200, execute(filter, "POST", "/auth/login", "10.0.0.1").getStatus());
    }

    @Test
    void passwordResetEndpointsHaveIndependentBucketsAndGetRequestsBypassLimit() throws Exception {
        AtomicLong clock = new AtomicLong(5_000L);
        SecurityAuditService audit = mock(SecurityAuditService.class);
        SensitiveEndpointRateLimitFilter filter =
                new SensitiveEndpointRateLimitFilter(10, 1, 60, audit, clock::get);

        assertEquals(200, execute(filter, "POST", "/forgot-password", "10.0.0.2").getStatus());
        assertEquals(429, execute(filter, "POST", "/forgot-password", "10.0.0.2").getStatus());
        assertEquals(200, execute(filter, "POST", "/reset-password", "10.0.0.2").getStatus());
        assertEquals(200, execute(filter, "POST", "/auth/otp", "10.0.0.3").getStatus());
        assertEquals(429, execute(filter, "POST", "/auth/otp", "10.0.0.3").getStatus());
        assertEquals(200, execute(filter, "GET", "/forgot-password", "10.0.0.2").getStatus());
    }

    private MockHttpServletResponse execute(
            SensitiveEndpointRateLimitFilter filter,
            String method,
            String path,
            String remoteIp) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        request.setRemoteAddr(remoteIp);
        MockHttpServletResponse response = new MockHttpServletResponse();
        filter.doFilter(request, response, new MockFilterChain());
        return response;
    }
}
