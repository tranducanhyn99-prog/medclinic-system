package com.medclinic;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.http.MediaType;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class SecurityHardeningIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private MeterRegistry meterRegistry;

    @Test
    void publicResponsesIncludeSecurityHeaders() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(header().string("Referrer-Policy", "strict-origin-when-cross-origin"))
                .andExpect(header().string("Permissions-Policy",
                        "camera=(), microphone=(), geolocation=()"))
                .andExpect(header().string("X-Content-Type-Options", "nosniff"));
    }

    @Test
    void healthIsPublicButMetricsRequireAdmin() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.components").doesNotExist());

        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/actuator/metrics").with(
                        SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("names")));
    }

    @Test
    void loginSuccessAndFailureAreAuditedAsMetrics() throws Exception {
        double failureBefore = authenticationCount("failure");
        double successBefore = authenticationCount("success");

        mockMvc.perform(post("/auth/login").with(csrf())
                        .param("username", "patient")
                        .param("password", "wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        mockMvc.perform(post("/auth/login").with(csrf())
                        .param("username", "patient")
                        .param("password", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"));

        assertEquals(failureBefore + 1.0, authenticationCount("failure"));
        assertEquals(successBefore + 1.0, authenticationCount("success"));
    }

    @Test
    void logoutIsAuditedAsMetric() throws Exception {
        double before = authenticationCount("logout");

        mockMvc.perform(post("/logout").with(csrf()).with(
                        SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR")))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout=true"));

        assertEquals(before + 1.0, authenticationCount("logout"));
    }

    @Test
    void bankCallbackIsPublicAndCsrfExemptButStillRejectedWithoutValidPaymentConfiguration() throws Exception {
        mockMvc.perform(post("/api/payments/bank/callback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-MedClinic-Signature", "00".repeat(32))
                        .content("""
                                {"eventId":"evt-security","transactionId":"txn-security",
                                 "transferCode":"MC1ABCDEF","amount":100000}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.accepted").value(false));
    }

    private double authenticationCount(String outcome) {
        var counter = meterRegistry.find("medclinic.security.authentication")
                .tag("outcome", outcome).counter();
        return counter == null ? 0.0 : counter.count();
    }
}
