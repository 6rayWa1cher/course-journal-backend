package com.a6raywa1cher.coursejournalbackend.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CourseTokenAuthentication extends AbstractAuthenticationToken {
    private String token;
    private final Object principal;

    public CourseTokenAuthentication(String token, Object principal) {
        super(null);
        this.token = token;
        this.principal = principal;
    }

    public CourseTokenAuthentication(String token, Object principal, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.token = token;
        this.principal = principal;
        super.setAuthenticated(true);
    }

    @Override
    public String getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public void setAuthenticated(boolean authenticated) {
        if (authenticated) {
            throw new IllegalArgumentException("Cannot set authenticated to true");
        }
        super.setAuthenticated(false);
    }

    @Override
    public void eraseCredentials() {
        this.token = null;
    }
}
