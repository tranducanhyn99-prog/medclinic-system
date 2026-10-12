package com.medclinic.service;

import com.medclinic.entity.User;
import com.medclinic.enums.RoleType;
import com.medclinic.repository.RoleRepository;
import com.medclinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GoogleAccountService {
    private final UserRepository users;
    private final RoleRepository roles;
    private final PasswordEncoder passwords;

    @Transactional
    public User resolve(OidcUser google) {
        String subject = google.getSubject();
        String rawEmail = google.getEmail();
        if (subject == null || subject.isBlank() || subject.length() > 255 || rawEmail == null
                || rawEmail.length() > 100 || !Boolean.TRUE.equals(google.getEmailVerified())) throw denied();
        String email = rawEmail.trim().toLowerCase(Locale.ROOT);
        String domain = google.getClaimAsString("hd");
        // Google does not guarantee current ownership of arbitrary third-party email addresses.
        if (!email.endsWith("@gmail.com") && (domain == null || domain.isBlank()
                || !email.endsWith("@" + domain.toLowerCase(Locale.ROOT)))) throw denied();

        User user = users.findByGoogleSubject(subject).orElse(null);
        if (user != null) {
            requirePatient(user);
            // Email changes need explicit recovery review; never move a medical account by email alone.
            if (!user.getEmail().equalsIgnoreCase(email)) throw denied();
            return user;
        }
        user = users.findByEmailIgnoreCaseForUpdate(email).orElse(null);
        if (user != null) {
            requirePatient(user);
            if (!Boolean.TRUE.equals(user.getEmailVerified()) || user.getGoogleSubject() != null) throw denied();
        } else {
            String fullName = google.getFullName();
            if (fullName == null || fullName.isBlank()) fullName = email;
            user = User.builder().username("google_" + UUID.randomUUID().toString().replace("-", ""))
                    .password(passwords.encode(UUID.randomUUID().toString().replace("-", "")
                            + UUID.randomUUID().toString().replace("-", "")))
                    .fullName(fullName.substring(0, Math.min(100, fullName.length())))
                    .email(email).emailVerified(true).otpBypass(false).isActive(true)
                    .role(roles.findByName(RoleType.ROLE_PATIENT).orElseThrow()).build();
        }
        user.setGoogleSubject(subject);
        try { return users.saveAndFlush(user); }
        catch (DataIntegrityViolationException exception) { throw denied(); }
    }

    private void requirePatient(User user) {
        if (!Boolean.TRUE.equals(user.getIsActive()) || user.getRole().getName() != RoleType.ROLE_PATIENT) throw denied();
    }

    private OAuth2AuthenticationException denied() {
        return new OAuth2AuthenticationException(new OAuth2Error("clinic_account_unavailable"),
                "Không thể dùng Google cho tài khoản này. Vui lòng đăng nhập bằng mật khẩu hoặc liên hệ phòng khám.");
    }
}
