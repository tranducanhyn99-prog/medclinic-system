package com.medclinic;

import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import com.medclinic.security.GoogleUserDetails;
import com.medclinic.service.GoogleAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.oidcLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(properties={"app.auth.google.enabled=true", "app.auth.google.client-id=test-client",
        "app.auth.google.client-secret=test-secret-not-live"})
@AutoConfigureMockMvc
@ActiveProfiles("dev")
class GoogleLoginIntegrationTest {
    @Autowired GoogleAccountService googleAccounts;
    @Autowired UserRepository users;
    @Autowired RoleRepository roles;
    @Autowired MockMvc mvc;

    @Test
    @Transactional
    void newGooglePatientHasClinicPrincipalAndExistingOwnerChecksWork() throws Exception {
        OidcUser google = identity("subject_"+UUID.randomUUID(), "patient_"+UUID.randomUUID()+"@gmail.com", true);
        User account = googleAccounts.resolve(google);
        assertEquals(RoleType.ROLE_PATIENT, account.getRole().getName());
        assertTrue(account.getEmailVerified());
        assertFalse(account.getOtpBypass());
        assertNull(account.getPhone());
        assertEquals(account.getId(), googleAccounts.resolve(google).getId());
        var principal = new GoogleUserDetails(account, google);
        assertEquals(account.getUsername(), principal.getName());
        mvc.perform(get("/patient/appointments").with(oidcLogin().oidcUser(principal)))
                .andExpect(status().isOk());
        mvc.perform(get("/admin/dashboard").with(oidcLogin().oidcUser(principal)))
                .andExpect(status().isForbidden());
    }

    @Test
    @Transactional
    void matchingVerifiedActiveEmailLinksExistingAccountWithoutChangingItsRole() {
        String email = "existing_"+UUID.randomUUID()+"@gmail.com";
        User existing = fixture(email, true, true);
        String password = existing.getPassword();
        OidcUser google = identity("existing_subject_"+UUID.randomUUID(), email, true);
        assertEquals(existing.getId(), googleAccounts.resolve(google).getId());
        assertEquals(RoleType.ROLE_PATIENT, existing.getRole().getName());
        assertEquals(google.getSubject(), users.findById(existing.getId()).orElseThrow().getGoogleSubject());
        assertEquals(password, users.findById(existing.getId()).orElseThrow().getPassword());
    }

    @Test
    @Transactional
    void unverifiedLockedOrDifferentBoundSubjectCannotBeLinked() {
        String pendingEmail = "pending_"+UUID.randomUUID()+"@gmail.com";
        fixture(pendingEmail, true, false);
        assertThrows(OAuth2AuthenticationException.class,
                () -> googleAccounts.resolve(identity("pending", pendingEmail, true)));
        String lockedEmail = "locked_"+UUID.randomUUID()+"@gmail.com";
        fixture(lockedEmail, false, true);
        assertThrows(OAuth2AuthenticationException.class,
                () -> googleAccounts.resolve(identity("locked", lockedEmail, true)));
        User bound = fixture("bound_"+UUID.randomUUID()+"@gmail.com", true, true);
        bound.setGoogleSubject("original-subject"); users.saveAndFlush(bound);
        assertThrows(OAuth2AuthenticationException.class,
                () -> googleAccounts.resolve(identity("wrong-subject", bound.getEmail(), true)));
    }

    @Test
    @Transactional
    void rejectsUnverifiedAndNonAuthoritativeEmail() {
        assertThrows(OAuth2AuthenticationException.class,
                () -> googleAccounts.resolve(identity("unverified", "test@gmail.com", false)));
        assertThrows(OAuth2AuthenticationException.class,
                () -> googleAccounts.resolve(identity("third-party", "test@example.test", true)));
    }

    @Test
    @Transactional
    void linksCaseInsensitivePatientEmailButNeverGrantsEmployeeRoles() {
        String token = UUID.randomUUID().toString();
        User patient = fixture("Mixed_" + token + "@gmail.com", true, true);
        assertEquals(patient.getId(), googleAccounts.resolve(identity("mixed_" + token,
                patient.getEmail().toLowerCase(java.util.Locale.ROOT), true)).getId());
        for (RoleType role : List.of(RoleType.ROLE_ADMIN, RoleType.ROLE_DOCTOR, RoleType.ROLE_STAFF)) {
            User employee = fixture(role + "_" + token + "@gmail.com", true, true);
            employee.setRole(roles.findByName(role).orElseThrow());
            users.saveAndFlush(employee);
            assertThrows(OAuth2AuthenticationException.class, () -> googleAccounts.resolve(
                    identity("employee_" + role + token, employee.getEmail(), true)));
            assertNull(users.findById(employee.getId()).orElseThrow().getGoogleSubject());
        }
    }

    @Test
    @Transactional
    void verifiedWorkspaceCanRegisterAndChangedEmailDoesNotMoveAnExistingMedicalAccount() {
        String subject = "workspace_" + UUID.randomUUID();
        String email = "patient_" + UUID.randomUUID() + "@example.test";
        OidcUser google = new DefaultOidcUser(List.of(new SimpleGrantedAuthority("SCOPE_openid")),
                new OidcIdToken("test-token", Instant.now(), Instant.now().plusSeconds(300),
                        Map.of("sub", subject, "email", email, "email_verified", true, "hd", "example.test")));
        User patient = googleAccounts.resolve(google);
        assertEquals(email, patient.getFullName());
        assertThrows(OAuth2AuthenticationException.class,
                () -> googleAccounts.resolve(identity(subject, "changed@gmail.com", true)));
        assertEquals(email, users.findById(patient.getId()).orElseThrow().getEmail());
    }

    @Test
    void googlePrincipalAlsoLosesAccessAfterLockAndCannotReuseTheSessionAfterUnlock() throws Exception {
        OidcUser google = identity("lock_" + UUID.randomUUID(), "lock_" + UUID.randomUUID() + "@gmail.com", true);
        User patient = googleAccounts.resolve(google);
        var principal = new GoogleUserDetails(patient, google);
        mvc.perform(get("/profile").with(oidcLogin().oidcUser(principal))).andExpect(status().isOk());
        User persisted = users.findById(patient.getId()).orElseThrow();
        persisted.setIsActive(false); persisted.setSecurityVersion(persisted.getSecurityVersion() + 1);
        users.saveAndFlush(persisted);
        mvc.perform(get("/patient/appointments").with(oidcLogin().oidcUser(principal)))
                .andExpect(redirectedUrl("/login?locked=true"));
        assertThrows(OAuth2AuthenticationException.class, () -> googleAccounts.resolve(google));
        persisted.setIsActive(true); users.saveAndFlush(persisted);
        mvc.perform(get("/patient/appointments").with(oidcLogin().oidcUser(principal)))
                .andExpect(redirectedUrl("/login?locked=true"));
        mvc.perform(get("/patient/appointments").with(oidcLogin().oidcUser(
                new GoogleUserDetails(googleAccounts.resolve(google), google)))).andExpect(status().isOk());
    }

    @Test
    void concurrentFirstGoogleRegistrationCreatesOnePatientOnly() throws Exception {
        String email = "race_" + UUID.randomUUID() + "@gmail.com";
        OidcUser google = identity("race_" + UUID.randomUUID(), email, true);
        var start = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.Callable<Long> attempt = () -> {
            assertTrue(start.await(10, java.util.concurrent.TimeUnit.SECONDS));
            try { return googleAccounts.resolve(google).getId(); }
            catch (OAuth2AuthenticationException rejectedRace) { return null; }
        };
        try (var executor = java.util.concurrent.Executors.newFixedThreadPool(2)) {
            var first = executor.submit(attempt); var second = executor.submit(attempt); start.countDown();
            Long one = first.get(20, java.util.concurrent.TimeUnit.SECONDS);
            Long two = second.get(20, java.util.concurrent.TimeUnit.SECONDS);
            assertTrue(one != null || two != null);
            Long owner = users.findByEmail(email).orElseThrow().getId();
            if (one != null) assertEquals(owner, one);
            if (two != null) assertEquals(owner, two);
            assertEquals(owner, googleAccounts.resolve(google).getId());
        }
    }

    @Test
    void loginPageShowsGoogleAndAuthorizationUsesStateNonceAndExpectedCallback() throws Exception {
        mvc.perform(get("/login")).andExpect(status().isOk())
                .andExpect(result -> assertEquals(1, org.springframework.util.StringUtils.countOccurrencesOf(
                        result.getResponse().getContentAsString(), "href=\"/oauth2/authorization/google\"")))
                .andExpect(content().string(containsString("Đăng nhập bằng Google")));
        mvc.perform(get("/register")).andExpect(status().isOk())
                .andExpect(result -> assertEquals(1, org.springframework.util.StringUtils.countOccurrencesOf(
                        result.getResponse().getContentAsString(), "href=\"/oauth2/authorization/google\"")))
                .andExpect(content().string(containsString("Đăng ký bằng Google")));
        mvc.perform(get("/oauth2/authorization/google")).andExpect(status().is3xxRedirection())
                .andExpect(header().string("Location", allOf(containsString("https://accounts.google.com"),
                        containsString("state="), containsString("nonce="), containsString("client_id=test-client"),
                        containsString("redirect_uri=http://localhost/login/oauth2/code/google"))));
        mvc.perform(get("/login/oauth2/code/google").param("code", "invalid").param("state", "forged"))
                .andExpect(redirectedUrl("/login?googleError=true"));
    }

    private User fixture(String email, boolean active, boolean verified) {
        return users.saveAndFlush(User.builder().username("google_test_"+UUID.randomUUID().toString().substring(0,8))
                .email(email).password("unused-test-password").fullName("Google test")
                .role(roles.findByName(RoleType.ROLE_PATIENT).orElseThrow()).isActive(active)
                .emailVerified(verified).build());
    }

    private OidcUser identity(String subject, String email, boolean verified) {
        return new DefaultOidcUser(List.of(new SimpleGrantedAuthority("SCOPE_openid")),
                new OidcIdToken("test-token", Instant.now(), Instant.now().plusSeconds(300),
                        Map.of("sub",subject,"email",email,"email_verified",verified,"name","Google Test")));
    }
}
