package com.medclinic;

import com.medclinic.config.GoogleLoginConfig;
import com.medclinic.service.GoogleAccountService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GoogleLoginConfigurationTest {
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withUserConfiguration(GoogleLoginConfig.class)
            .withBean(GoogleAccountService.class, () -> mock(GoogleAccountService.class));

    @Test
    void disabledGoogleNeedsNoCredentials() {
        runner.withPropertyValues("app.auth.google.enabled=false").run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).doesNotHaveBean(ClientRegistrationRepository.class);
        });
    }

    @Test
    void explicitlyEnabledGoogleFailsStartupIfCredentialsAreMissing() {
        runner.withPropertyValues("app.auth.google.enabled=true").run(context -> {
            assertThat(context).hasFailed();
            assertThat(context.getStartupFailure()).hasRootCauseMessage(
                    "Google login requires GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.");
        });
    }
}
