package com.microservice.billing.service;

import com.microservice.billing.config.ApiKeyAuthenticationToken;
import com.microservice.billing.config.ApiKeyCustomerMapper;
import com.microservice.billing.exception.CustomerAccessDeniedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerContextServiceTest {

    @Mock
    private ApiKeyCustomerMapper apiKeyCustomerMapper;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private CustomerContextService customerContextService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void shouldReturnCustomerIdWhenAuthenticated() {
        String apiKey = "test-api-key";
        String customerId = "customer-123";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken(apiKey);

        when(securityContext.getAuthentication()).thenReturn(token);
        when(apiKeyCustomerMapper.getCustomerIdForApiKey(apiKey)).thenReturn(customerId);

        String result = customerContextService.getCurrentCustomerId();

        assertEquals(customerId, result);
    }

    @Test
    void shouldReturnNullWhenNotAuthenticated() {
        when(securityContext.getAuthentication()).thenReturn(null);

        String result = customerContextService.getCurrentCustomerId();

        assertNull(result);
    }

    @Test
    void shouldReturnNullWhenWrongAuthenticationType() {
        Authentication otherAuth = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(otherAuth);

        String result = customerContextService.getCurrentCustomerId();

        assertNull(result);
    }

    @Test
    void shouldAllowAccessWhenNoMappingConfigured() {
        String requestedCustomerId = "customer-123";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken("api-key");

        when(securityContext.getAuthentication()).thenReturn(token);
        when(apiKeyCustomerMapper.getCustomerIdForApiKey("api-key")).thenReturn(null);

        assertDoesNotThrow(() -> customerContextService.verifyCustomerAccess(requestedCustomerId));
    }

    @Test
    void shouldAllowAccessWhenCustomerIdsMatch() {
        String customerId = "customer-123";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken("api-key");

        when(securityContext.getAuthentication()).thenReturn(token);
        when(apiKeyCustomerMapper.getCustomerIdForApiKey("api-key")).thenReturn(customerId);

        assertDoesNotThrow(() -> customerContextService.verifyCustomerAccess(customerId));
    }

    @Test
    void shouldDenyAccessWhenCustomerIdsDontMatch() {
        String authenticatedCustomerId = "customer-123";
        String requestedCustomerId = "customer-456";
        ApiKeyAuthenticationToken token = new ApiKeyAuthenticationToken("api-key");

        when(securityContext.getAuthentication()).thenReturn(token);
        when(apiKeyCustomerMapper.getCustomerIdForApiKey("api-key")).thenReturn(authenticatedCustomerId);

        CustomerAccessDeniedException exception = assertThrows(CustomerAccessDeniedException.class,
                () -> customerContextService.verifyCustomerAccess(requestedCustomerId));

        assertEquals("Access denied: You can only access your own data", exception.getMessage());
        assertEquals(authenticatedCustomerId, exception.getAuthenticatedCustomerId());
        assertEquals(requestedCustomerId, exception.getRequestedCustomerId());
    }
}

