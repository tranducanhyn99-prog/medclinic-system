package com.medclinic;

import com.medclinic.enums.RoleType;
import com.medclinic.entity.User;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class LockedAccountSessionIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;

    @Test
    void lockRevokesEveryOldSessionEvenAfterUnlockAndDoesNotAffectOtherUsers() throws Exception {
        User patient = users.save(User.builder().username("locked_" + UUID.randomUUID())
                .email(UUID.randomUUID()+"@example.test").fullName("Locked session test")
                .password(encoder.encode("password123")).otpBypass(true)
                .role(roles.findByName(RoleType.ROLE_PATIENT).orElseThrow()).build());
        MockHttpSession first = login(patient.getUsername(), "password123");
        MockHttpSession second = login(patient.getUsername(), "password123");
        MockHttpSession admin = login("admin", "123456");
        mvc.perform(post("/admin/users/" + patient.getId() + "/toggle").session(admin).with(csrf()))
                .andExpect(status().is3xxRedirection());
        assertFalse(users.findById(patient.getId()).orElseThrow().getIsActive());
        mvc.perform(get("/patient/appointments").session(first)).andExpect(redirectedUrl("/login?locked=true"));
        assertTrue(first.isInvalid());
        mvc.perform(get("/admin/dashboard").session(admin)).andExpect(status().isOk());
        mvc.perform(post("/auth/login").with(csrf()).param("username", patient.getUsername())
                        .param("password", "password123")).andExpect(redirectedUrl("/login?error=true"));
        mvc.perform(post("/admin/users/" + patient.getId() + "/toggle").session(admin).with(csrf()))
                .andExpect(status().is3xxRedirection());
        mvc.perform(post("/profile/change-password").session(second).with(csrf())
                        .param("oldPassword", "password123").param("newPassword", "newpassword123")
                        .param("confirmPassword", "newpassword123"))
                .andExpect(redirectedUrl("/login?locked=true"));
        assertTrue(second.isInvalid());
        assertTrue(encoder.matches("password123", users.findById(patient.getId()).orElseThrow().getPassword()));
        mvc.perform(get("/patient/appointments").session(login(patient.getUsername(), "password123")))
                .andExpect(status().isOk());
    }

    private MockHttpSession login(String username, String password) throws Exception {
        return (MockHttpSession) mvc.perform(post("/auth/login").with(csrf())
                        .param("username", username).param("password", password)).andReturn().getRequest().getSession(false);
    }
}
