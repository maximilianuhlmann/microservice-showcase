package com.microservice.billing.config;

import com.microservice.billing.repository.ApiKeyRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class ApiKeyCustomerMapper {

    private final ApiKeyRepository apiKeyRepository;
    private final Map<String, String> apiKeyToCustomerMap = new HashMap<>();
    private final boolean useDatabase;

    public ApiKeyCustomerMapper(
            ApiKeyRepository apiKeyRepository,
            @Value("${api.key.customer.mapping:}") String mappingConfig,
            @Value("${api.key.use-database:true}") boolean useDatabase) {
        this.apiKeyRepository = apiKeyRepository;
        this.useDatabase = useDatabase;
        
        if (!useDatabase && mappingConfig != null && !mappingConfig.trim().isEmpty()) {
            String[] mappings = mappingConfig.split(",");
            for (String mapping : mappings) {
                String[] parts = mapping.trim().split("=");
                if (parts.length == 2) {
                    String apiKey = parts[0].trim();
                    String customerId = parts[1].trim();
                    apiKeyToCustomerMap.put(apiKey, customerId);
                }
            }
        }
    }

    public String getCustomerIdForApiKey(String apiKey) {
        if (apiKey == null) {
            return null;
        }
        
        if (useDatabase) {
            return apiKeyRepository.findByApiKeyAndActiveTrue(apiKey.trim())
                    .map(com.microservice.billing.domain.ApiKey::getCustomerId)
                    .orElse(null);
        }
        
        return apiKeyToCustomerMap.get(apiKey.trim());
    }

    public boolean isApiKeyMapped(String apiKey) {
        if (apiKey == null) {
            return false;
        }
        
        if (useDatabase) {
            return apiKeyRepository.findByApiKeyAndActiveTrue(apiKey.trim()).isPresent();
        }
        
        return apiKeyToCustomerMap.containsKey(apiKey.trim());
    }
}

