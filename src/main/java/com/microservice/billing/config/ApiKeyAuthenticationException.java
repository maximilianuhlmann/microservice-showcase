package com.microservice.billing.config;

import org.springframework.security.core.AuthenticationException;

/**
 * Exception thrown when API key authentication fails.
 */
public class ApiKeyAuthenticationException extends AuthenticationException {

    public ApiKeyAuthenticationException(String msg) {
        super(msg);
    }

    public ApiKeyAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}



