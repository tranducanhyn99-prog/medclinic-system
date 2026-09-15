package com.medclinic.config;

import com.medclinic.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return (request, response, authentication) -> {
            var authorities = authentication.getAuthorities();
            String redirectUrl = "/";
            for (var authority : authorities) {
                String role = authority.getAuthority();
                if (role.equals("ROLE_ADMIN")) {
                    redirectUrl = "/admin/dashboard";
                    break;
                } else if (role.equals("ROLE_DOCTOR")) {
                    redirectUrl = "/doctor/desk";
                    break;
                } else if (role.equals("ROLE_STAFF")) {
                    redirectUrl = "/staff/reception";
                    break;
                } else if (role.equals("ROLE_PATIENT")) {
                    redirectUrl = "/patient/appointments";
                    break;
                }
            }
            response.sendRedirect(redirectUrl);
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/", "/home", "/about", "/specialties/**", "/doctors/**",
                    "/login", "/register", "/css/**", "/js/**", "/images/**",
                    "/webjars/**", "/h2-console/**", "/error/**", "/api/public/**",
                    "/ws/**"
                ).permitAll()
                .requestMatchers("/patient/**").hasAnyRole("PATIENT", "ADMIN")
                .requestMatchers("/doctor/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/staff/**").hasAnyRole("STAFF", "ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(customAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("medclinic-remember-me-secret-key-2026")
                .tokenValiditySeconds(86400 * 14) // 14 days
                .userDetailsService(userDetailsService)
            )
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/h2-console/**", "/ws/**", "/api/**")
            )
            .headers(headers -> headers
                .frameOptions(frame -> frame.sameOrigin()) // Allow H2 Console iframe
            );

        return http.build();
    }
}
