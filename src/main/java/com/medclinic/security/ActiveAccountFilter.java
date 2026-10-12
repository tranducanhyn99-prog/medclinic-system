package com.medclinic.security;

import com.medclinic.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/** Rechecks persisted status: a session snapshot cannot observe an admin locking the user. */
public class ActiveAccountFilter extends OncePerRequestFilter {
    private final UserRepository users;

    public ActiveAccountFilter(UserRepository users) { this.users = users; }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails details
                && !users.existsByIdAndIsActiveTrueAndSecurityVersion(details.getId(), details.getSecurityVersion())) {
            new SecurityContextLogoutHandler().logout(request, response, authentication);
            response.addHeader("Set-Cookie", "remember-me=; Max-Age=0; Path="
                    + (request.getContextPath().isEmpty() ? "/" : request.getContextPath()) + "; HttpOnly; SameSite=Lax");
            if (request.getRequestURI().startsWith(request.getContextPath() + "/api/")) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            } else {
                response.sendRedirect(request.getContextPath() + "/login?locked=true");
            }
            return;
        }
        chain.doFilter(request, response);
    }
}
