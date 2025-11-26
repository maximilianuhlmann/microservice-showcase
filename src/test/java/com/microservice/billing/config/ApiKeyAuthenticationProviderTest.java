package com.microservice.billing.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationProviderTest {

    @Mock
    private ApiKeyValidator apiKeyValidator;

    @InjectMocks
    private ApiKeyAuthenticationProvider provider;

    @Test
    void shouldRejectNullApiKey() {
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(null);

        ApiKeyAuthenticationException exception = assertThrows(
                ApiKeyAuthenticationException.class,
                () -> provider.authenticate(token)
        );

        assertEquals("API key is required", exception.getMessage());
        verify(apiKeyValidator, never()).isValid(anyString());
    }

    @Test
    void shouldRejectEmptyApiKey() {
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken("   ");

        ApiKeyAuthenticationException exception = assertThrows(
                ApiKeyAuthenticationException.class,
                () -> provider.authenticate(token)
        );

        assertEquals("API key is required", exception.getMessage());
    }

    @Test
    void shouldAllowWhenApiKeyNotRequired() {
        String apiKey = "any-key";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(apiKey);

        when(apiKeyValidator.isApiKeyRequired()).thenReturn(false);

        Authentication result = provider.authenticate(token);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(apiKey, ((ApiKeyAuthenticationToken) result).getApiKey());
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_API_CLIENT", authorities.iterator().next().getAuthority());
        verify(apiKeyValidator, never()).isValid(anyString());
    }

    @Test
    void shouldRejectInvalidApiKey() {
        String apiKey = "invalid-key";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(apiKey);

        when(apiKeyValidator.isApiKeyRequired()).thenReturn(true);
        when(apiKeyValidator.isValid(apiKey)).thenReturn(false);

        ApiKeyAuthenticationException exception = assertThrows(
                ApiKeyAuthenticationException.class,
                () -> provider.authenticate(token)
        );

        assertEquals("Invalid API key", exception.getMessage());
        verify(apiKeyValidator).isValid(apiKey);
    }

    @Test
    void shouldAuthenticateValidApiKey() {
        String apiKey = "valid-key";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(apiKey);

        when(apiKeyValidator.isApiKeyRequired()).thenReturn(true);
        when(apiKeyValidator.isValid(apiKey)).thenReturn(true);

        Authentication result = provider.authenticate(token);

        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        assertEquals(apiKey, ((ApiKeyAuthenticationToken) result).getApiKey());
        Collection<? extends GrantedAuthority> authorities = result.getAuthorities();
        assertEquals(1, authorities.size());
        assertEquals("ROLE_API_CLIENT", authorities.iterator().next().getAuthority());
        verify(apiKeyValidator).isValid(apiKey);
    }

    @Test
    void shouldSupportApiKeyAuthenticationToken() {
        assertTrue(provider.supports(ApiKeyAuthenticationToken.class));
        assertTrue(provider.supports(ApiKeyAuthenticationToken.class.asSubclass(Authentication.class)));
    }

    @Test
    void shouldNotSupportOtherAuthenticationTypes() {
        assertFalse(provider.supports(String.class));
        assertFalse(provider.supports(Object.class));
    }
}

