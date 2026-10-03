package com.medclinic;

import com.medclinic.repository.DoctorProfileRepository;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class AuthenticatedPortalRenderIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private DoctorProfileRepository doctorProfileRepository;

    @Test
    void rendersRepresentativeAuthenticatedPortalsForBrowserQa() throws Exception {
        snapshot("patient-booking", "/patient/booking",
                SecurityMockMvcRequestPostProcessors.user("patient-browser").roles("PATIENT"));
        snapshot("staff-reception", "/staff/reception",
                SecurityMockMvcRequestPostProcessors.user("staff-browser").roles("STAFF"));
        var doctor = doctorProfileRepository.findAll().getFirst();
        snapshot("doctor-desk", "/doctor/desk",
                SecurityMockMvcRequestPostProcessors.user(new CustomUserDetails(doctor.getUser())));
        snapshot("admin-dashboard", "/admin/dashboard",
                SecurityMockMvcRequestPostProcessors.user("admin-browser").roles("ADMIN"));
        snapshot("admin-doctors", "/admin/doctors",
                SecurityMockMvcRequestPostProcessors.user("admin-browser").roles("ADMIN"));
        snapshot("admin-specialties", "/admin/specialties",
                SecurityMockMvcRequestPostProcessors.user("admin-browser").roles("ADMIN"));
        snapshot("admin-medicines", "/admin/medicines",
                SecurityMockMvcRequestPostProcessors.user("admin-browser").roles("ADMIN"));
        snapshot("staff-billing", "/staff/billing",
                SecurityMockMvcRequestPostProcessors.user("staff-browser").roles("STAFF"));
    }

    private void snapshot(String name, String url, RequestPostProcessor principal) throws Exception {
        MvcResult result = mockMvc.perform(get(url).with(principal))
                .andExpect(status().isOk())
                .andReturn();
        String html = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertTrue(html.contains("<html"), name + " must render full HTML");
        assertTrue(html.contains("id=\"main-content\""), name + " must use the shared portal layout");
        Path dir = Path.of("target", "portal-snapshots");
        Files.createDirectories(dir);
        Files.writeString(dir.resolve(name + ".html"), html, StandardCharsets.UTF_8);
    }
}
