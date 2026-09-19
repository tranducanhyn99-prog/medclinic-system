package com.medclinic;

import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("performance")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PortalPerformanceBaselineIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private DoctorProfileRepository doctorProfileRepository;

    @Test
    void recordsLocalMockMvcBaselineForAuthenticatedPortalPages() throws Exception {
        measure("PERF-01 /", "/", null);
        measure("PERF-02 /patient/booking", "/patient/booking",
                SecurityMockMvcRequestPostProcessors.user("patient-benchmark").roles("PATIENT"));
        measure("PERF-03 /staff/reception", "/staff/reception",
                SecurityMockMvcRequestPostProcessors.user("staff-benchmark").roles("STAFF"));
        var doctor = doctorProfileRepository.findAll().getFirst();
        measure("PERF-04 /doctor/desk", "/doctor/desk",
                SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(doctor.getUser())));
    }

    private void measure(String id, String url, RequestPostProcessor principal) throws Exception {
        for (int i = 0; i < 2; i++) {
            request(url, principal);
        }

        List<Long> samples = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            long started = System.nanoTime();
            request(url, principal);
            samples.add(System.nanoTime() - started);
        }

        Collections.sort(samples);
        System.out.printf("%s local MockMvc baseline: p50=%.3f ms p95=%.3f ms max=%.3f ms%n",
                id, millis(samples.get(4)), millis(samples.get(9)), millis(samples.get(9)));
    }

    private void request(String url, RequestPostProcessor principal) throws Exception {
        var request = get(url);
        if (principal != null) {
            request.with(principal);
        }
        mockMvc.perform(request).andExpect(status().isOk());
    }

    private double millis(long nanos) {
        return nanos / 1_000_000.0;
    }
}
