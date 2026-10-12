package com.medclinic.security;

import com.medclinic.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketSessionDecorator;
import java.io.IOException;

/** An already-open socket must also stop receiving private queue updates after account lock. */
public class ActiveAccountWebSocketSession extends WebSocketSessionDecorator {
    private final UserRepository users;
    public ActiveAccountWebSocketSession(WebSocketSession session, UserRepository users) { super(session); this.users = users; }
    @Override
    public void sendMessage(WebSocketMessage<?> message) throws IOException {
        if (getPrincipal() instanceof Authentication authentication
                && authentication.getPrincipal() instanceof CustomUserDetails details
                && !users.existsByIdAndIsActiveTrueAndSecurityVersion(details.getId(), details.getSecurityVersion())) {
            close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        super.sendMessage(message);
    }
}
