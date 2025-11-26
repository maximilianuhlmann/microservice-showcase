package com.microservice.billing.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter for API key authentication.
 * Extracts the X-API-Key header and delegates to the AuthenticationManager.
 * Uses Spring Security's AuthenticationProvider pattern for proper integration.
 */
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private static final String API_KEY_HEADER = "X-API-Key";
    private final AuthenticationManager authenticationManager;
    private final ApiKeyValidator apiKeyValidator;

    public ApiKeyAuthenticationFilter(AuthenticationManager authenticationManager, ApiKeyValidator apiKeyValidator) {
        this.authenticationManager = authenticationManager;
        this.apiKeyValidator = apiKeyValidator;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        
        // Only process /api/** endpoints
        String path = request.getRequestURI();
        if (!path.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }
        
        String apiKey = request.getHeader(API_KEY_HEADER);
        
        // If API keys are not required and no key provided, allow anonymous access
        if (!apiKeyValidator.isApiKeyRequired() && (apiKey == null || apiKey.trim().isEmpty())) {
            chain.doFilter(request, response);
            return;
        }
        
        // If API keys are required but not provided, reject
        if (apiKeyValidator.isApiKeyRequired() && (apiKey == null || apiKey.trim().isEmpty())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Missing API key. Provide X-API-Key header.\"}");
            return;
        }
        
        // If API key is provided, attempt authentication using AuthenticationManager
        if (apiKey != null && !apiKey.trim().isEmpty()) {
            try {
                ApiKeyAuthenticationToken authRequest = new ApiKeyAuthenticationToken(apiKey);
                Authentication authResult = authenticationManager.authenticate(authRequest);
                SecurityContextHolder.getContext().setAuthentication(authResult);
            } catch (AuthenticationException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"" + e.getMessage() + "\"}");
                return;
            }
        }
        
        chain.doFilter(request, response);
    }
}

