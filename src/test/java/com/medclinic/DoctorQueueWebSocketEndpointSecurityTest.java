package com.medclinic;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class DoctorQueueWebSocketEndpointSecurityTest {

    @Autowired private MockMvc mockMvc;

    @Test
    void websocketEndpointRequiresAuthenticatedSession() throws Exception {
        mockMvc.perform(get("/ws/info"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    void authenticatedDoctorCanReachSockJsInfoEndpoint() throws Exception {
        mockMvc.perform(get("/ws/info").with(
                        SecurityMockMvcRequestPostProcessors.user("doctor").roles("DOCTOR")))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("websocket")));
    }
}
