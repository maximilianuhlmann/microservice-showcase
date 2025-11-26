package com.microservice.billing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyAuthenticationFilterTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private ApiKeyValidator apiKeyValidator;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private ApiKeyAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldSkipNonApiEndpoints() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/h2-console");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldAllowAnonymousWhenApiKeyNotRequired() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/usage-events");
        when(apiKeyValidator.isApiKeyRequired()).thenReturn(false);
        when(request.getHeader("X-API-Key")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void shouldRejectWhenApiKeyRequiredButMissing() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/usage-events");
        when(apiKeyValidator.isApiKeyRequired()).thenReturn(true);
        when(request.getHeader("X-API-Key")).thenReturn(null);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");
        assertTrue(stringWriter.toString().contains("Missing API key"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldRejectWhenApiKeyRequiredButEmpty() throws ServletException, IOException {
        when(request.getRequestURI()).thenReturn("/api/v1/usage-events");
        when(apiKeyValidator.isApiKeyRequired()).thenReturn(true);
        when(request.getHeader("X-API-Key")).thenReturn("   ");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldAuthenticateValidApiKey() throws ServletException, IOException {
        String apiKey = "valid-api-key";
        when(request.getRequestURI()).thenReturn("/api/v1/usage-events");
        when(apiKeyValidator.isApiKeyRequired()).thenReturn(true);
        when(request.getHeader("X-API-Key")).thenReturn(apiKey);

        ApiKeyAuthenticationToken authenticatedToken = new ApiKeyAuthenticationToken(apiKey);
        when(authenticationManager.authenticate(any(ApiKeyAuthenticationToken.class)))
                .thenReturn(authenticatedToken);

        filter.doFilterInternal(request, response, filterChain);

        ArgumentCaptor<ApiKeyAuthenticationToken> tokenCaptor = ArgumentCaptor.forClass(ApiKeyAuthenticationToken.class);
        verify(authenticationManager).authenticate(tokenCaptor.capture());
        assertEquals(apiKey, tokenCaptor.getValue().getApiKey());
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void shouldRejectInvalidApiKey() throws ServletException, IOException {
        String apiKey = "invalid-api-key";
        when(request.getRequestURI()).thenReturn("/api/v1/usage-events");
        when(apiKeyValidator.isApiKeyRequired()).thenReturn(true);
        when(request.getHeader("X-API-Key")).thenReturn(apiKey);

        ApiKeyAuthenticationException exception = new ApiKeyAuthenticationException("Invalid API key");
        when(authenticationManager.authenticate(any(ApiKeyAuthenticationToken.class)))
                .thenThrow(exception);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        filter.doFilterInternal(request, response, filterChain);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        assertTrue(stringWriter.toString().contains("Invalid API key"));
        verify(filterChain, never()).doFilter(any(), any());
    }

    @Test
    void shouldHandleApiKeyWhenNotRequired() throws ServletException, IOException {
        String apiKey = "any-api-key";
        when(request.getRequestURI()).thenReturn("/api/v1/usage-events");
        when(apiKeyValidator.isApiKeyRequired()).thenReturn(false);
        when(request.getHeader("X-API-Key")).thenReturn(apiKey);

        ApiKeyAuthenticationToken authenticatedToken = new ApiKeyAuthenticationToken(apiKey);
        when(authenticationManager.authenticate(any(ApiKeyAuthenticationToken.class)))
                .thenReturn(authenticatedToken);

        filter.doFilterInternal(request, response, filterChain);

        verify(authenticationManager).authenticate(any(ApiKeyAuthenticationToken.class));
        verify(filterChain).doFilter(request, response);
    }
}

