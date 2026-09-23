package com.medclinic;

import com.medclinic.entity.Role;
import com.medclinic.entity.User;
import com.medclinic.enums.Gender;
import com.medclinic.enums.AuthChallengeType;
import com.medclinic.exception.BusinessLogicException;
import com.medclinic.enums.RoleType;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.service.EmailService;
import com.medclinic.service.AuthVerificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class OtpAuthenticationIntegrationTest {
    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private AuthVerificationService authVerificationService;
    @MockBean private EmailService emailService;

    @BeforeEach
    void resetEmailMock() {
        reset(emailService);
    }

    @Test
    void registrationRequiresOtpThenActivatesAndLogsUserIn() throws Exception {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "otp_" + suffix;
        String email = username + "@example.test";

        MvcResult registration = mockMvc.perform(post("/register").with(csrf())
                        .param("username", username)
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("fullName", "OTP Patient")
                        .param("email", email)
                        .param("phone", "093" + String.format("%07d", Math.floorMod(suffix.hashCode(), 10_000_000)))
                        .param("gender", "MALE")
                        .param("dateOfBirth", "2000-01-02"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        String redirect = registration.getResponse().getRedirectedUrl();
        assertNotNull(redirect);
        assertTrue(redirect.startsWith("/auth/otp?challenge="));
        long challengeId = Long.parseLong(redirect.substring(redirect.indexOf('=') + 1));

        User pending = userRepository.findByUsername(username).orElseThrow();
        assertFalse(pending.getIsActive());
        assertFalse(pending.getEmailVerified());
        assertFalse(pending.getOtpBypass());

        ArgumentCaptor<String> codeCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendAuthenticationOtpEmail(eq(email), eq("OTP Patient"),
                codeCaptor.capture(), anyString(), eq(5L), eq("đăng ký tài khoản"));
        assertTrue(codeCaptor.getValue().matches("\\d{6}"));

        MvcResult verified = mockMvc.perform(post("/auth/otp").with(csrf())
                        .param("challengeId", Long.toString(challengeId))
                        .param("code", codeCaptor.getValue()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"))
                .andReturn();

        User active = userRepository.findByUsername(username).orElseThrow();
        assertTrue(active.getIsActive());
        assertTrue(active.getEmailVerified());
        MockHttpSession session = (MockHttpSession) verified.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("OTP Patient")));
    }

    @Test
    void loginMagicLinkCreatesAuthenticatedSession() throws Exception {
        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String username = "magic_" + suffix;
        String email = username + "@example.test";
        User user = userRepository.save(User.builder()
                .username(username)
                .password(passwordEncoder.encode("password123"))
                .fullName("Magic Link Patient")
                .email(email)
                .phone(null)
                .gender(Gender.FEMALE)
                .dateOfBirth(LocalDate.of(1999, 3, 4))
                .role(patientRole)
                .emailVerified(true)
                .otpBypass(false)
                .isActive(true)
                .build());

        MvcResult login = mockMvc.perform(post("/auth/login").with(csrf())
                        .param("username", username)
                        .param("password", "password123"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        var pendingSession = login.getRequest().getSession(false);
        assertTrue(pendingSession == null || pendingSession.getAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY) == null,
                "OTP-pending login must not be authenticated");
        ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendAuthenticationOtpEmail(eq(email), eq("Magic Link Patient"),
                anyString(), urlCaptor.capture(), eq(5L), eq("đăng nhập"));
        String confirmationUrl = urlCaptor.getValue();
        String token = confirmationUrl.substring(confirmationUrl.indexOf("token=") + 6);

        MvcResult confirmed = mockMvc.perform(get("/auth/verify-email").param("token", token))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/patient/appointments"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) confirmed.getRequest().getSession(false);
        assertNotNull(session);
        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Magic Link Patient")));
        assertFalse(user.getOtpBypass());
    }
    @Test
    void resendOtpInvalidatesPreviousChallenge() {
        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "resend_" + suffix + "@example.test";
        User user = userRepository.save(User.builder()
                .username("resend_" + suffix).password(passwordEncoder.encode("password123"))
                .fullName("Resend Patient").email(email).role(patientRole)
                .emailVerified(true).otpBypass(false).isActive(true).build());

        var first = authVerificationService.issueChallenge(user.getId(), AuthChallengeType.LOGIN);
        ArgumentCaptor<String> firstCode = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendAuthenticationOtpEmail(eq(email), anyString(), firstCode.capture(),
                anyString(), eq(5L), eq("đăng nhập"));
        reset(emailService);

        var second = authVerificationService.resendChallenge(first.challengeId());
        ArgumentCaptor<String> secondCode = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendAuthenticationOtpEmail(eq(email), anyString(), secondCode.capture(),
                anyString(), eq(5L), eq("đăng nhập"));
        assertThrows(BusinessLogicException.class,
                () -> authVerificationService.verifyCode(first.challengeId(), firstCode.getValue()));
        assertEquals(user.getId(), authVerificationService.verifyCode(second.challengeId(), secondCode.getValue()).user().getId());
    }

    @Test
    void resendRequiresTheSessionThatStartedTheChallenge() throws Exception {
        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "session_" + suffix + "@example.test";
        User user = userRepository.save(User.builder()
                .username("session_" + suffix).password(passwordEncoder.encode("password123"))
                .fullName("Session Patient").email(email).role(patientRole)
                .emailVerified(true).otpBypass(false).isActive(true).build());

        MvcResult login = mockMvc.perform(post("/auth/login").with(csrf())
                        .param("username", user.getUsername()).param("password", "password123"))
                .andExpect(status().is3xxRedirection()).andReturn();
        long challengeId = Long.parseLong(login.getResponse().getRedirectedUrl().split("=")[1]);
        MockHttpSession ownerSession = (MockHttpSession) login.getRequest().getSession(false);
        assertNotNull(ownerSession);
        clearInvocations(emailService);

        mockMvc.perform(post("/auth/otp/resend").with(csrf())
                        .param("challengeId", Long.toString(challengeId)))
                .andExpect(redirectedUrl("/login"));
        mockMvc.perform(post("/auth/otp/resend").session(new MockHttpSession()).with(csrf())
                        .param("challengeId", Long.toString(challengeId)))
                .andExpect(redirectedUrl("/login"));
        verifyNoInteractions(emailService);

        assertEquals(challengeId, authVerificationService.getChallengeInfo(challengeId).challengeId());
        MvcResult resent = mockMvc.perform(post("/auth/otp/resend").session(ownerSession).with(csrf())
                        .param("challengeId", Long.toString(challengeId)))
                .andExpect(status().is3xxRedirection()).andReturn();
        assertTrue(resent.getResponse().getRedirectedUrl().startsWith("/auth/otp?challenge="));
        verify(emailService).sendAuthenticationOtpEmail(eq(email), anyString(),
                anyString(), anyString(), eq(5L), eq("đăng nhập"));
    }

    @Test
    void resendLimitKeepsLatestChallengeUsable() {
        Role patientRole = roleRepository.findByName(RoleType.ROLE_PATIENT).orElseThrow();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String email = "limit_" + suffix + "@example.test";
        User user = userRepository.save(User.builder()
                .username("limit_" + suffix).password(passwordEncoder.encode("password123"))
                .fullName("Limit Patient").email(email).role(patientRole)
                .emailVerified(true).otpBypass(false).isActive(true).build());

        var first = authVerificationService.issueChallenge(user.getId(), AuthChallengeType.LOGIN);
        var second = authVerificationService.resendChallenge(first.challengeId());
        var third = authVerificationService.resendChallenge(second.challengeId());
        assertThrows(BusinessLogicException.class,
                () -> authVerificationService.resendChallenge(third.challengeId()));
        assertEquals(third.challengeId(), authVerificationService.getChallengeInfo(third.challengeId()).challengeId());
        verify(emailService, times(3)).sendAuthenticationOtpEmail(eq(email), anyString(),
                anyString(), anyString(), anyLong(), anyString());
    }

}
