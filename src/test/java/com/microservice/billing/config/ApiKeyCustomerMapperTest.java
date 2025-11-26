package com.microservice.billing.config;

import com.microservice.billing.domain.ApiKey;
import com.microservice.billing.repository.ApiKeyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiKeyCustomerMapperTest {

    @Mock
    private ApiKeyRepository apiKeyRepository;

    @Test
    void shouldGetCustomerIdFromDatabase() {
        String apiKey = "db-api-key";
        String customerId = "customer-123";
        ApiKey apiKeyEntity = ApiKey.builder()
                .id(1L)
                .apiKey(apiKey)
                .customerId(customerId)
                .active(true)
                .build();

        when(apiKeyRepository.findByApiKeyAndActiveTrue(apiKey)).thenReturn(Optional.of(apiKeyEntity));

        ApiKeyCustomerMapper mapperWithDb = new ApiKeyCustomerMapper(apiKeyRepository, "", true);
        String result = mapperWithDb.getCustomerIdForApiKey(apiKey);

        assertEquals(customerId, result);
        verify(apiKeyRepository).findByApiKeyAndActiveTrue(apiKey);
    }

    @Test
    void shouldReturnNullWhenApiKeyNotFoundInDatabase() {
        String apiKey = "non-existent-key";

        when(apiKeyRepository.findByApiKeyAndActiveTrue(apiKey)).thenReturn(Optional.empty());

        ApiKeyCustomerMapper mapperWithDb = new ApiKeyCustomerMapper(apiKeyRepository, "", true);
        String result = mapperWithDb.getCustomerIdForApiKey(apiKey);

        assertNull(result);
        verify(apiKeyRepository).findByApiKeyAndActiveTrue(apiKey);
    }

    @Test
    void shouldGetCustomerIdFromConfig() {
        String apiKey = "config-api-key";
        String customerId = "customer-456";
        String mappingConfig = apiKey + "=" + customerId;

        ApiKeyCustomerMapper mapperWithConfig = new ApiKeyCustomerMapper(apiKeyRepository, mappingConfig, false);
        String result = mapperWithConfig.getCustomerIdForApiKey(apiKey);

        assertEquals(customerId, result);
        verify(apiKeyRepository, never()).findByApiKeyAndActiveTrue(anyString());
    }

    @Test
    void shouldHandleMultipleConfigMappings() {
        String mappingConfig = "key1=customer-1,key2=customer-2,key3=customer-3";

        ApiKeyCustomerMapper mapperWithConfig = new ApiKeyCustomerMapper(apiKeyRepository, mappingConfig, false);

        assertEquals("customer-1", mapperWithConfig.getCustomerIdForApiKey("key1"));
        assertEquals("customer-2", mapperWithConfig.getCustomerIdForApiKey("key2"));
        assertEquals("customer-3", mapperWithConfig.getCustomerIdForApiKey("key3"));
    }

    @Test
    void shouldReturnNullForNullApiKey() {
        ApiKeyCustomerMapper mapperWithDb = new ApiKeyCustomerMapper(apiKeyRepository, "", true);
        assertNull(mapperWithDb.getCustomerIdForApiKey(null));

        ApiKeyCustomerMapper mapperWithConfig = new ApiKeyCustomerMapper(apiKeyRepository, "key=customer", false);
        assertNull(mapperWithConfig.getCustomerIdForApiKey(null));
    }

    @Test
    void shouldTrimApiKey() {
        String apiKey = "  api-key-with-spaces  ";
        String customerId = "customer-123";
        ApiKey apiKeyEntity = ApiKey.builder()
                .id(1L)
                .apiKey("api-key-with-spaces")
                .customerId(customerId)
                .active(true)
                .build();

        when(apiKeyRepository.findByApiKeyAndActiveTrue("api-key-with-spaces"))
                .thenReturn(Optional.of(apiKeyEntity));

        ApiKeyCustomerMapper mapperWithDb = new ApiKeyCustomerMapper(apiKeyRepository, "", true);
        String result = mapperWithDb.getCustomerIdForApiKey(apiKey);

        assertEquals(customerId, result);
        verify(apiKeyRepository).findByApiKeyAndActiveTrue("api-key-with-spaces");
    }

    @Test
    void shouldCheckApiKeyMappedInDatabase() {
        String apiKey = "mapped-key";
        ApiKey apiKeyEntity = ApiKey.builder()
                .id(1L)
                .apiKey(apiKey)
                .customerId("customer-1")
                .active(true)
                .build();

        when(apiKeyRepository.findByApiKeyAndActiveTrue(apiKey)).thenReturn(Optional.of(apiKeyEntity));

        ApiKeyCustomerMapper mapperWithDb = new ApiKeyCustomerMapper(apiKeyRepository, "", true);
        assertTrue(mapperWithDb.isApiKeyMapped(apiKey));
        assertFalse(mapperWithDb.isApiKeyMapped("non-existent"));
    }

    @Test
    void shouldCheckApiKeyMappedInConfig() {
        String mappingConfig = "key1=customer-1";

        ApiKeyCustomerMapper mapperWithConfig = new ApiKeyCustomerMapper(apiKeyRepository, mappingConfig, false);
        assertTrue(mapperWithConfig.isApiKeyMapped("key1"));
        assertFalse(mapperWithConfig.isApiKeyMapped("key2"));
    }
}

