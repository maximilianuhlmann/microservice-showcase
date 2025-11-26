package com.microservice.billing.service;

import com.microservice.billing.config.ApiKeyAuthenticationToken;
import com.microservice.billing.config.ApiKeyCustomerMapper;
import com.microservice.billing.exception.CustomerAccessDeniedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomerContextService {

    private final ApiKeyCustomerMapper apiKeyCustomerMapper;

    public String getCurrentCustomerId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (!(authentication instanceof ApiKeyAuthenticationToken)) {
            return null;
        }
        
        ApiKeyAuthenticationToken token = (ApiKeyAuthenticationToken) authentication;
        String apiKey = token.getApiKey();
        
        return apiKeyCustomerMapper.getCustomerIdForApiKey(apiKey);
    }

    public void verifyCustomerAccess(String requestedCustomerId) {
        String authenticatedCustomerId = getCurrentCustomerId();
        
        if (authenticatedCustomerId == null) {
            log.debug("No customer mapping found for API key, allowing access (may be admin or unmapped key)");
            return;
        }
        
        if (!authenticatedCustomerId.equals(requestedCustomerId)) {
            log.warn("Customer access denied: authenticated customer '{}' attempted to access customer '{}'", 
                    authenticatedCustomerId, requestedCustomerId);
            throw new CustomerAccessDeniedException(authenticatedCustomerId, requestedCustomerId);
        }
    }
}

