package com.medclinic.config;

import com.medclinic.security.CustomUserDetailsService;
import com.medclinic.security.SecurityAuditService;
import com.medclinic.security.SensitiveEndpointRateLimitFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.header.writers.StaticHeadersWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;
    private final SensitiveEndpointRateLimitFilter rateLimitFilter;
    private final SecurityAuditService securityAuditService;
    private final com.medclinic.repository.UserRepository userRepository;

    @Bean
    public PasswordEncoder passwordEncoder() {
        PasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                if (!com.medclinic.util.PasswordPolicy.fitsBcrypt(rawPassword)) {
                    throw new IllegalArgumentException(com.medclinic.util.PasswordPolicy.MESSAGE);
                }
                return bcrypt.encode(rawPassword);
            }
            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                // Keep existing BCrypt hashes and short demo passwords compatible, never truncate input.
                return com.medclinic.util.PasswordPolicy.fitsBcrypt(rawPassword)
                        && bcrypt.matches(rawPassword, encodedPassword);
            }
            @Override public boolean upgradeEncoding(String encodedPassword) { return bcrypt.upgradeEncoding(encodedPassword); }
        };
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public LogoutSuccessHandler customLogoutSuccessHandler() {
        return (request, response, authentication) -> {
            String principal = authentication == null ? "unknown" : authentication.getName();
            securityAuditService.logout(principal, request.getRemoteAddr());
            response.sendRedirect("/login?logout=true");
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
            org.springframework.beans.factory.ObjectProvider<org.springframework.security.oauth2.client.registration.ClientRegistrationRepository> googleClients,
            org.springframework.beans.factory.ObjectProvider<org.springframework.security.oauth2.client.userinfo.OAuth2UserService<org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest, org.springframework.security.oauth2.core.oidc.user.OidcUser>> googleUsers,
            com.medclinic.service.AuthVerificationService verification) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.GET, "/patient/booking").permitAll()
                .requestMatchers(
                    "/", "/home", "/about", "/specialties/**", "/doctors/**",
                    "/login", "/register", "/auth/**", "/oauth2/authorization/google", "/login/oauth2/code/google", "/forgot-password", "/reset-password", "/css/**", "/js/**", "/images/**",
                    "/manifest.json", "/sw.js", "/offline.html", "/favicon.ico", "/vendor/ui/**",
                    "/webjars/**", "/h2-console/**", "/error", "/error/**", "/api/public/**"
                ).permitAll()
                .requestMatchers("/api/payments/bank/callback").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/ws/**").authenticated()
                .requestMatchers("/patient/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers("/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    String path = request.getRequestURI().substring(request.getContextPath().length());
                    String accept = request.getHeader("Accept");
                    if (path.startsWith("/api/") || path.startsWith("/actuator/") || (accept != null && accept.contains("json"))) {
                        response.setContentType("application/json");
                        response.setCharacterEncoding("UTF-8");
                        response.getWriter().write("{\"code\":\"FORBIDDEN\",\"message\":\"Bạn không có quyền thực hiện yêu cầu.\"}");
                    } else {
                        request.getRequestDispatcher("/error/403").forward(request, response);
                    }
                })
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessHandler(customLogoutSuccessHandler())
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "remember-me")
                .permitAll()
            )
            .csrf(csrf -> csrf
                // Browser API requests use the same session cookie and must retain CSRF protection.
                .ignoringRequestMatchers("/h2-console/**", "/ws/**", "/api/payments/bank/callback")
            )
            .sessionManagement(session -> session
                .sessionFixation(fixation -> fixation.migrateSession())
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Allow H2 Console iframe
                .addHeaderWriter(new StaticHeadersWriter(
                        "Referrer-Policy", "strict-origin-when-cross-origin"))
                .addHeaderWriter((request, response) -> response.setHeader("Permissions-Policy",
                        "camera=" + (request.getRequestURI().equals(request.getContextPath() + "/staff/reception")
                                ? "(self)" : "()") + ", microphone=(), geolocation=()"))
            )
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new com.medclinic.security.ActiveAccountFilter(userRepository),
                    org.springframework.security.web.access.intercept.AuthorizationFilter.class);

        if (googleClients.getIfAvailable() != null) {
            http.oauth2Login(oauth -> oauth.loginPage("/login")
                    // Google proves identity; only the OTP controller may persist a clinic login.
                    .securityContextRepository(new org.springframework.security.web.context.NullSecurityContextRepository())
                    .userInfoEndpoint(info -> info.oidcUserService(googleUsers.getObject()))
                    .successHandler((request, response, authentication) -> {
                        var principal = (com.medclinic.security.GoogleUserDetails) authentication.getPrincipal();
                        org.springframework.security.core.context.SecurityContextHolder.clearContext();
                        new org.springframework.security.web.context.HttpSessionSecurityContextRepository().saveContext(
                                org.springframework.security.core.context.SecurityContextHolder.createEmptyContext(), request, response);
                        var session = request.getSession(true);
                        session.removeAttribute("PENDING_OTP_CHALLENGE_ID");
                        try {
                            var issued = verification.issueGoogleChallenge(principal.getId());
                            session.setAttribute("PENDING_OTP_CHALLENGE_ID", issued.challengeId());
                            response.sendRedirect(request.getContextPath() + "/auth/otp?challenge=" + issued.challengeId());
                        } catch (RuntimeException exception) {
                            securityAuditService.authenticationFailure("google", request.getRemoteAddr(), "OTP_CHALLENGE_FAILED");
                            response.sendRedirect(request.getContextPath() + "/login?googleError=true");
                        }
                    })
                    .failureHandler((request, response, exception) -> {
                        String reason = exception instanceof org.springframework.security.oauth2.core.OAuth2AuthenticationException oauthError
                                ? oauthError.getError().getErrorCode() : "GOOGLE_LOGIN_FAILED";
                        securityAuditService.authenticationFailure("google", request.getRemoteAddr(), reason);
                        response.sendRedirect(request.getContextPath() + "/login?googleError=true");
                    }));
        }
        return http.build();
    }
}
