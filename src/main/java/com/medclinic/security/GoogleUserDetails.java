package com.medclinic.security;

import com.medclinic.entity.User;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcUserInfo;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import java.util.Map;

/** Uses clinic roles and ID so existing @AuthenticationPrincipal/owner checks also work for Google. */
public class GoogleUserDetails extends CustomUserDetails implements OidcUser {
    private final OidcUser google;
    public GoogleUserDetails(User user, OidcUser google) { super(user); this.google = google; }
    @Override public String getName() { return getUsername(); }
    @Override public Map<String, Object> getAttributes() { return google.getAttributes(); }
    @Override public Map<String, Object> getClaims() { return google.getClaims(); }
    @Override public OidcIdToken getIdToken() { return google.getIdToken(); }
    @Override public OidcUserInfo getUserInfo() { return google.getUserInfo(); }
}
