package com.medclinic.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.DependsOn;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.JdbcTemplate;
import com.medclinic.service.GoogleAccountService;
import com.medclinic.security.GoogleUserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.config.oauth2.client.CommonOAuth2Provider;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;

@Configuration
@ConditionalOnProperty(name = "app.auth.google.enabled", havingValue = "true")
public class GoogleLoginConfig {
    @Bean
    ClientRegistrationRepository googleClients(
            @Value("${app.auth.google.client-id:}") String clientId,
            @Value("${app.auth.google.client-secret:}") String clientSecret) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            throw new IllegalStateException("Google login requires GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.");
        }
        return new InMemoryClientRegistrationRepository(CommonOAuth2Provider.GOOGLE.getBuilder("google")
                .clientId(clientId).clientSecret(clientSecret)
                .scope("openid", "email", "profile")
                .issuerUri("https://accounts.google.com")
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}").build());
    }

    @Bean
    OAuth2UserService<OidcUserRequest, OidcUser> googleOidcUserService(GoogleAccountService accounts) {
        var google = new OidcUserService();
        google.setRetrieveUserInfo(request -> false); // Verified ID-token claims suffice; no Google API access is needed.
        return request -> {
            var verified = google.loadUser(request);
            return new GoogleUserDetails(accounts.resolve(verified), verified);
        };
    }

    @Bean
    @Profile("dev & !sqlserver")
    @DependsOn("entityManagerFactory")
    InitializingBean googleSubjectConstraint(JdbcTemplate jdbc) {
        return () -> jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS ux_users_google_subject ON users(google_subject)");
    }
}
