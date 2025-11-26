package com.microservice.billing.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Validates API keys against configured valid keys.
 * Supports multiple API keys (comma-separated) for different clients.
 */
@Component
@Slf4j
public class ApiKeyValidator {

    private final Set<String> validApiKeys;

    public ApiKeyValidator(@Value("${api.keys:}") String apiKeys) {
        // Parse comma-separated API keys from configuration
        this.validApiKeys = Arrays.stream(apiKeys.split(","))
                .map(String::trim)
                .filter(key -> !key.isEmpty())
                .collect(Collectors.toSet());
        
        if (validApiKeys.isEmpty()) {
            log.warn("No API keys configured. API authentication will be disabled. " +
                    "Set 'api.keys' property to enable API key authentication.");
        } else {
            log.info("API key authentication enabled with {} key(s)", validApiKeys.size());
        }
    }

    /**
     * Checks if API key authentication is required (keys are configured).
     * 
     * @return true if API keys are configured and required, false otherwise
     */
    public boolean isApiKeyRequired() {
        return !validApiKeys.isEmpty();
    }

    /**
     * Validates if the provided API key is valid.
     * 
     * @param apiKey The API key to validate
     * @return true if the key is valid, false otherwise
     */
    public boolean isValid(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }
        
        // If no keys are configured, consider any key as valid (backward compatibility)
        if (validApiKeys.isEmpty()) {
            return true;
        }
        
        boolean valid = validApiKeys.contains(apiKey.trim());
        if (!valid) {
            log.warn("Invalid API key attempted: {}", maskApiKey(apiKey));
        }
        return valid;
    }

    private String maskApiKey(String apiKey) {
        if (apiKey == null || apiKey.length() <= 8) {
            return "***";
        }
        return apiKey.substring(0, 4) + "..." + apiKey.substring(apiKey.length() - 4);
    }
}

