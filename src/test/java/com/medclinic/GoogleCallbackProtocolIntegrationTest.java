package com.medclinic;

import com.medclinic.repository.UserRepository;
import com.medclinic.security.CustomUserDetails;
import com.medclinic.service.EmailService;
import com.medclinic.repository.AuthVerificationChallengeRepository;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.jwt.JwtDecoderFactory;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties = {"app.auth.google.enabled=true", "app.auth.google.client-id=protocol-test-client",
        "app.auth.google.client-secret=test-secret-not-live", "app.security.rate-limit.password-reset-max-requests=100"})
@ActiveProfiles("dev")
@AutoConfigureMockMvc
@Import(GoogleCallbackProtocolIntegrationTest.TestProvider.class)
class GoogleCallbackProtocolIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;
    @Autowired AuthVerificationChallengeRepository challenges;
    @MockitoBean OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> tokenClient;
    @MockitoBean EmailService emailService;
    private static final KeyPair KEYS = keys();

    @Test
    void newGooglePatientNeedsOtpBeforeClinicSessionAndContinuesBooking() throws Exception {
        var session = new MockHttpSession();
        mvc.perform(get("/login").param("next", "/patient/booking").session(session)).andExpect(status().isOk());
        session.setAttribute("PENDING_OTP_CHALLENGE_ID", 999L);
        String email = "callback_" + UUID.randomUUID() + "@gmail.com";
        String oldId = session.getId();
        var pending = startGoogle(email, session);
        session = pending.session();
        long challengeId = pending.challengeId();
        assertNotEquals(oldId, session.getId());
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        assertEquals(challengeId, session.getAttribute("PENDING_OTP_CHALLENGE_ID"));
        assertEquals(com.medclinic.enums.AuthChallengeType.LOGIN, challenges.findById(challengeId).orElseThrow().getType());
        mvc.perform(get("/profile").session(session)).andExpect(redirectedUrl("/login"));
        mvc.perform(get("/patient/appointments").session(session)).andExpect(redirectedUrl("/login"));
        String code = otpCode(email);
        mvc.perform(post("/auth/otp").session(session).param("challengeId", Long.toString(challengeId)).param("code", code))
                .andExpect(status().isForbidden());
        String pendingId = session.getId();
        mvc.perform(post("/auth/otp").session(session).with(csrf())
                        .param("challengeId", Long.toString(challengeId)).param("code", code))
                .andExpect(redirectedUrl("/patient/booking"));
        var established = session;
        assertNotEquals(pendingId, established.getId());
        assertNull(established.getAttribute("PENDING_OTP_CHALLENGE_ID"));
        var context = (SecurityContext) established.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertInstanceOf(CustomUserDetails.class, context.getAuthentication().getPrincipal());
        var principal = (CustomUserDetails) context.getAuthentication().getPrincipal();
        assertEquals(users.findByEmail(email).orElseThrow().getId(), principal.getId());
        mvc.perform(get("/patient/appointments").session(established)).andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Google callback test")))
                .andExpect(content().string(org.hamcrest.Matchers.not(org.hamcrest.Matchers.containsString(principal.getUsername()))));
        mvc.perform(get("/admin/dashboard").session(established)).andExpect(status().isForbidden());
    }

    @Test
    void existingGoogleAccountStillNeedsOtpEvenWhenDemoBypassIsSet() throws Exception {
        String email = "existing_callback_" + UUID.randomUUID() + "@gmail.com";
        long first = startGoogle(email, new MockHttpSession()).challengeId();
        var patient = users.findByEmail(email).orElseThrow();
        patient.setOtpBypass(true);
        users.saveAndFlush(patient);
        reset(emailService);
        var session = new MockHttpSession();
        var pending = startGoogle(email, session);
        session = pending.session();
        long next = pending.challengeId();
        assertNotEquals(first, next);
        assertNotNull(challenges.findById(first).orElseThrow().getConsumedAt());
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        mvc.perform(post("/auth/otp").session(session).with(csrf())
                        .param("challengeId", Long.toString(next)).param("code", otpCode(email)))
                .andExpect(redirectedUrl("/patient/appointments"));
        var context = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertEquals(patient.getId(), ((CustomUserDetails) context.getAuthentication().getPrincipal()).getId());
        assertTrue(users.findById(patient.getId()).orElseThrow().getOtpBypass());
    }

    @Test
    void wrongSessionAndWrongCodeCannotLoginAndResendReplacesTheCode() throws Exception {
        String email = "resend_callback_" + UUID.randomUUID() + "@gmail.com";
        var session = new MockHttpSession();
        var pending = startGoogle(email, session);
        session = pending.session();
        long first = pending.challengeId();
        String oldCode = otpCode(email);
        mvc.perform(post("/auth/otp").session(new MockHttpSession()).with(csrf())
                        .param("challengeId", Long.toString(first)).param("code", oldCode))
                .andExpect(redirectedUrl("/login"));
        assertEquals(0, challenges.findById(first).orElseThrow().getAttempts());
        mvc.perform(post("/auth/otp").session(session).with(csrf())
                        .param("challengeId", Long.toString(first)).param("code", "invalid"))
                .andExpect(redirectedUrl("/auth/otp?challenge=" + first));
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        reset(emailService);
        var resent = mvc.perform(post("/auth/otp/resend").session(session).with(csrf())
                        .param("challengeId", Long.toString(first))).andExpect(status().is3xxRedirection()).andReturn();
        long next = Long.parseLong(resent.getResponse().getRedirectedUrl().split("=")[1]);
        assertNotEquals(first, next);
        assertNotNull(challenges.findById(first).orElseThrow().getConsumedAt());
        mvc.perform(post("/auth/otp").session(session).with(csrf())
                        .param("challengeId", Long.toString(first)).param("code", oldCode))
                .andExpect(redirectedUrl("/login"));
        mvc.perform(post("/auth/otp").session(session).with(csrf())
                        .param("challengeId", Long.toString(next)).param("code", otpCode(email)))
                .andExpect(redirectedUrl("/patient/appointments"));
    }

    @Test
    void lockingGooglePatientDuringOtpCannotReactivateOrLoginTheAccount() throws Exception {
        String email = "locked_callback_" + UUID.randomUUID() + "@gmail.com";
        var session = new MockHttpSession();
        var pending = startGoogle(email, session);
        session = pending.session();
        long challenge = pending.challengeId();
        String code = otpCode(email);
        var patient = users.findByEmail(email).orElseThrow();
        patient.setIsActive(false);
        patient.setSecurityVersion(patient.getSecurityVersion() + 1);
        users.saveAndFlush(patient);
        mvc.perform(post("/auth/otp").session(session).with(csrf())
                        .param("challengeId", Long.toString(challenge)).param("code", code))
                .andExpect(redirectedUrl("/auth/otp?challenge=" + challenge));
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        assertFalse(users.findById(patient.getId()).orElseThrow().getIsActive());
    }

    @Test
    void challengeFailureCannotLeaveGoogleAuthenticated() throws Exception {
        var session = new MockHttpSession();
        var authorization = authorize(session);
        String email = "failure_callback_" + UUID.randomUUID() + "@gmail.com";
        respondWith(token(email, authorization.get("nonce"), KEYS));
        doThrow(new IllegalStateException("test delivery failure")).when(emailService)
                .sendAuthenticationOtpEmail(anyString(), anyString(), anyString(), anyString(), anyLong(), anyString());
        var callback = mvc.perform(get("/login/oauth2/code/google").session(session)
                        .param("state", authorization.get("state")).param("code", "test-code"))
                .andExpect(redirectedUrl("/login?googleError=true")).andReturn();
        session = (MockHttpSession) callback.getRequest().getSession(false);
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        assertNull(session.getAttribute("PENDING_OTP_CHALLENGE_ID"));
        mvc.perform(get("/profile").session(session)).andExpect(redirectedUrl("/login"));
    }

    @Test
    void callbackWithWrongNonceCannotCreateOrAuthenticateAPatient() throws Exception {
        rejectToken(false);
    }

    @Test
    void callbackWithForgedSignatureCannotCreateOrAuthenticateAPatient() throws Exception {
        rejectToken(true);
    }

    private void rejectToken(boolean forgedSignature) throws Exception {
        var session = new MockHttpSession();
        Map<String, String> authorization = authorize(session);
        String email = "rejected_" + UUID.randomUUID() + "@gmail.com";
        respondWith(token(email, forgedSignature ? authorization.get("nonce") : "wrong-nonce",
                forgedSignature ? keys() : KEYS));
        mvc.perform(get("/login/oauth2/code/google").session(session)
                        .param("state", authorization.get("state")).param("code", "test-code"))
                .andExpect(redirectedUrl("/login?googleError=true"));
        assertFalse(users.existsByEmail(email));
        assertNull(session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY));
        verifyNoInteractions(emailService);
    }

    private GooglePending startGoogle(String email, MockHttpSession session) throws Exception {
        var authorization = authorize(session);
        respondWith(token(email, authorization.get("nonce"), KEYS));
        var callback = mvc.perform(get("/login/oauth2/code/google").session(session)
                        .param("state", authorization.get("state")).param("code", "test-code"))
                .andExpect(status().is3xxRedirection()).andReturn();
        String redirect = callback.getResponse().getRedirectedUrl();
        assertNotNull(redirect);
        assertTrue(redirect.startsWith("/auth/otp?challenge="));
        return new GooglePending((MockHttpSession) callback.getRequest().getSession(false),
                Long.parseLong(redirect.split("=")[1]));
    }

    private record GooglePending(MockHttpSession session, long challengeId) {}

    private String otpCode(String email) {
        var code = ArgumentCaptor.forClass(String.class);
        verify(emailService).sendAuthenticationOtpEmail(eq(email), eq("Google callback test"), code.capture(),
                anyString(), eq(5L), eq("đăng nhập"));
        assertTrue(code.getValue().matches("\\d{6}"));
        return code.getValue();
    }

    private Map<String, String> authorize(MockHttpSession session) throws Exception {
        String location = mvc.perform(get("/oauth2/authorization/google").session(session))
                .andExpect(status().is3xxRedirection()).andReturn().getResponse().getRedirectedUrl();
        assertNotNull(location);
        return java.util.Arrays.stream(URI.create(location).getRawQuery().split("&"))
                .map(part -> part.split("=", 2)).collect(Collectors.toMap(part -> part[0],
                        part -> URLDecoder.decode(part[1], StandardCharsets.UTF_8)));
    }

    private void respondWith(String idToken) {
        when(tokenClient.getTokenResponse(any())).thenReturn(OAuth2AccessTokenResponse.withToken("test-access")
                .tokenType(OAuth2AccessToken.TokenType.BEARER).expiresIn(300).scopes(Set.of("openid"))
                .additionalParameters(Map.of("id_token", idToken)).build());
    }

    private String token(String email, String nonce, KeyPair signingKeys) throws Exception {
        Instant now = Instant.now();
        var claims = new JWTClaimsSet.Builder().issuer("https://accounts.google.com").subject("google_" + email)
                .audience("protocol-test-client").issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(300)))
                .claim("nonce", nonce).claim("email", email).claim("email_verified", true).claim("name", "Google callback test").build();
        var jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.RS256), claims);
        jwt.sign(new RSASSASigner((RSAPrivateKey) signingKeys.getPrivate()));
        return jwt.serialize();
    }

    private static KeyPair keys() {
        try { var generator = KeyPairGenerator.getInstance("RSA"); generator.initialize(2048); return generator.generateKeyPair(); }
        catch (java.security.GeneralSecurityException error) { throw new IllegalStateException(error); }
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class TestProvider {
        @Bean
        JwtDecoderFactory<ClientRegistration> localGoogleTestKeys() {
            return registration -> {
                var decoder = NimbusJwtDecoder.withPublicKey((RSAPublicKey) KEYS.getPublic()).build();
                decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("https://accounts.google.com"));
                return decoder;
            };
        }
    }
}
