package com.medclinic;

import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class PasswordLoginBoundaryIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired PasswordEncoder encoder;

    @ParameterizedTest
    @ValueSource(strings = {"ascii", "unicode"})
    void authenticationRejectsExtraSuffixButAcceptsExact72BytePassword(String kind) throws Exception {
        String password = kind.equals("ascii") ? "a".repeat(72) : "á".repeat(36);
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "boundary-" + suffix;
        users.save(User.builder().username(username).email(username + "@example.test").fullName("Password test fixture")
                .password(encoder.encode(password)).isActive(true).emailVerified(true).otpBypass(true)
                .role(roles.findByName(RoleType.ROLE_PATIENT).orElseThrow()).build());
        var denied = mvc.perform(post("/auth/login").param("username", username)
                        .param("password", password + "b").with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/login?error=true")).andReturn();
        var deniedSession = denied.getRequest().getSession(false);
        assertTrue(deniedSession == null || deniedSession.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) == null);
        mvc.perform(post("/auth/login").param("username", username).param("password", password).with(csrf()))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/patient/appointments"));
    }
}
