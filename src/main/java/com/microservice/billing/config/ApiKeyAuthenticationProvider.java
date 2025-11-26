package com.microservice.billing.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Authentication provider for API key authentication.
 * Validates API keys using the ApiKeyValidator.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationProvider implements AuthenticationProvider {

    private final ApiKeyValidator apiKeyValidator;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        ApiKeyAuthenticationToken token = (ApiKeyAuthenticationToken) authentication;
        String apiKey = token.getApiKey();

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new ApiKeyAuthenticationException("API key is required");
        }

        // If no keys are configured, allow all (development mode)
        if (!apiKeyValidator.isApiKeyRequired()) {
            log.debug("No API keys configured, allowing request");
            return new ApiKeyAuthenticationToken(
                    apiKey,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
            );
        }

        // Validate the API key
        if (!apiKeyValidator.isValid(apiKey)) {
            throw new ApiKeyAuthenticationException("Invalid API key");
        }

        // Return authenticated token
        return new ApiKeyAuthenticationToken(
                apiKey,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_API_CLIENT"))
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ApiKeyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

