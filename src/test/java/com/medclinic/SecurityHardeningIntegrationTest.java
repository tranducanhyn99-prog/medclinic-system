package com.medclinic;

import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
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
    @Test
    void anonymousBrowserCanLoadBundledUiWithoutAuthenticationRedirects() throws Exception {
        for (String file : new String[]{"bootstrap.min.css", "bootstrap-icons.css", "geist.css", "sweetalert2.min.css"}) {
            mockMvc.perform(get("/vendor/ui/" + file)).andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith("text/css"));
        }
        for (String file : new String[]{"bootstrap.bundle.min.js", "sweetalert2.all.min.js",
                "html5-qrcode.min.js", "sockjs.min.js", "stomp.umd.min.js",
                "fonts/Geist-Variable.woff2", "fonts/GeistMono-Variable.woff2", "fonts/bootstrap-icons.woff2"}) {
            mockMvc.perform(get("/vendor/ui/" + file)).andExpect(status().isOk());
        }
        mockMvc.perform(get("/doctor/desk")).andExpect(status().is3xxRedirection());
        mockMvc.perform(get("/actuator/metrics")).andExpect(status().is3xxRedirection());
    }
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
    void receptionCanRequestItsOwnCameraAndOfflinePageIsPublic() throws Exception {
        mockMvc.perform(get("/staff/reception").with(
                        SecurityMockMvcRequestPostProcessors.user("staff").roles("STAFF")))
                .andExpect(status().isOk())
                .andExpect(header().string("Permissions-Policy",
                        "camera=(self), microphone=(), geolocation=()"));
        mockMvc.perform(get("/offline.html"))
                .andExpect(status().isOk())
                .andExpect(result -> org.junit.jupiter.api.Assertions.assertTrue(
                        result.getResponse().getContentAsString(java.nio.charset.StandardCharsets.UTF_8)
                                .contains("Chưa có kết nối mạng")));
        mockMvc.perform(get("/user-content/avatars/private-patient.png"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void deniedPortalAccessUsesMedClinicRecoveryPage() throws Exception {
        mockMvc.perform(get("/admin/dashboard").with(
                        SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR")))
                .andExpect(status().isForbidden())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl("/error/403"));
        mockMvc.perform(get("/error/403").with(
                        SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR")))
                .andExpect(status().isForbidden())
                .andExpect(content().string(containsString("Tài khoản hiện tại không có quyền mở trang này")))
                .andExpect(content().string(containsString("Về bàn khám")))
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Whitelabel Error Page"))));
    }

    @Test
    void apiForbiddenRemainsJsonAndMissingWebPageIs404() throws Exception {
        mockMvc.perform(get("/actuator/metrics").accept(MediaType.APPLICATION_JSON).with(
                        SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("FORBIDDEN"));
        mockMvc.perform(get("/this-page-does-not-exist").with(
                        SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR")))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("Không tìm thấy trang")));
        mockMvc.perform(get("/error").requestAttr(jakarta.servlet.RequestDispatcher.ERROR_STATUS_CODE, 500))
                .andExpect(status().isInternalServerError())
                .andExpect(content().string(containsString("Vui lòng thử lại sau")));
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
