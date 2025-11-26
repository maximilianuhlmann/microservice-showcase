package com.microservice.billing.config;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.Collections;

/**
 * Authentication token for API key authentication.
 * This token is used by Spring Security's authentication mechanism.
 */
public class ApiKeyAuthenticationToken extends AbstractAuthenticationToken {

    private final String apiKey;

    public ApiKeyAuthenticationToken(String apiKey) {
        super(Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT")));
        this.apiKey = apiKey;
        setAuthenticated(false);
    }

    public ApiKeyAuthenticationToken(String apiKey, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.apiKey = apiKey;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return apiKey;
    }

    public String getApiKey() {
        return (String) getPrincipal();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ApiKeyAuthenticationToken that = (ApiKeyAuthenticationToken) o;
        return java.util.Objects.equals(apiKey, that.apiKey);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(super.hashCode(), apiKey);
    }
}



