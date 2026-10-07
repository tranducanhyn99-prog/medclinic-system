package com.medclinic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PublicPageRenderIntegrationTest {
    @Autowired private MockMvc mockMvc;

    @Test
    void anonymousFaviconUsesTheExistingLogoWithCorrectMimeAndCaching() throws Exception {
        byte[] logo = new org.springframework.core.io.ClassPathResource("static/images/logo.svg").getContentAsByteArray();
        mockMvc.perform(get("/favicon.ico")).andExpect(status().isOk())
                .andExpect(content().contentType("image/svg+xml"))
                .andExpect(content().bytes(logo))
                .andExpect(header().string("Cache-Control", "max-age=86400, public"));
    }

    @Test
    void publicPagesRenderTheirCompleteBodiesWithDevBootstrap() throws Exception {
        mockMvc.perform(get("/")).andExpect(status().isOk()).andExpect(view().name("home/index"));
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(content().string(containsString("data-username=\"patient\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"))
                .andExpect(content().string(containsString("id=\"registerTitle\"")))
                .andExpect(content().string(containsString("name=\"_csrf\"")));
    }
}
