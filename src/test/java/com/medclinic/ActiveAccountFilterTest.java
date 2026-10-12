package com.medclinic;

import com.medclinic.entity.User;
import com.medclinic.repository.UserRepository;
import com.medclinic.security.ActiveAccountFilter;
import com.medclinic.security.CustomUserDetails;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActiveAccountFilterTest {
    @Test
    void alreadyOpenWebSocketStopsPrivateUpdatesAfterAccountLock() throws Exception {
        var users = mock(UserRepository.class);
        var user = new CustomUserDetails(User.builder().id(78L).securityVersion(3).build());
        var socket = mock(org.springframework.web.socket.WebSocketSession.class);
        when(socket.getPrincipal()).thenReturn(org.springframework.security.authentication.UsernamePasswordAuthenticationToken
                .authenticated(user, null, List.of()));
        when(users.existsByIdAndIsActiveTrueAndSecurityVersion(78L,3)).thenReturn(true, false);
        var secured = new com.medclinic.security.ActiveAccountWebSocketSession(socket, users);
        var first = new org.springframework.web.socket.TextMessage("first-update");
        secured.sendMessage(first);
        secured.sendMessage(new org.springframework.web.socket.TextMessage("private-after-lock"));
        verify(socket).sendMessage(first);
        verify(socket).close(org.springframework.web.socket.CloseStatus.POLICY_VIOLATION);
        verify(socket, times(1)).sendMessage(any());
    }
    @Test
    void staleRememberMePrincipalIsRevokedAndCookieClearedWithoutPassingToController() throws Exception {
        var users = mock(UserRepository.class);
        var details = new CustomUserDetails(User.builder().id(77L).securityVersion(2).build());
        var authentication = new RememberMeAuthenticationToken("test-key", details, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            var request = new MockHttpServletRequest("POST", "/api/public/test");
            var session = new MockHttpSession(); request.setSession(session);
            var response = new MockHttpServletResponse();
            var chain = new MockFilterChain();
            new ActiveAccountFilter(users).doFilter(request, response, chain);
            assertEquals(401, response.getStatus());
            assertTrue(session.isInvalid());
            var cookie = response.getCookie("remember-me");
            assertNotNull(cookie);
            assertEquals("", cookie.getValue());
            assertEquals(0, cookie.getMaxAge());
            assertNull(SecurityContextHolder.getContext().getAuthentication());
            assertNull(chain.getRequest());
            verify(users).existsByIdAndIsActiveTrueAndSecurityVersion(77L, 2);
        } finally { SecurityContextHolder.clearContext(); }
    }
}
