package com.microservice.billing.config;

import com.microservice.billing.mapper.BillingRecordMapper;
import com.microservice.billing.mapper.UsageEventMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

/**
 * Test configuration for service layer tests.
 * 
 * Provides mock mapper beans with @Primary to override the real MapStruct-generated
 * implementations. This configuration does NOT perform any component scanning - scanning
 * is controlled exclusively by BillingServiceTestConfiguration.
 * 
 * This follows the common pattern for testing with MapStruct in Spring Boot:
 * - Use @MockBean in controller tests (which use @WebMvcTest)
 * - Use @Primary mock beans in service tests (which use @SpringBootTest)
 */
@TestConfiguration
public class ServiceTestConfig {

    @Bean
    @Primary
    public BillingRecordMapper billingRecordMapper() {
        return Mockito.mock(BillingRecordMapper.class);
    }

    @Bean
    @Primary
    public UsageEventMapper usageEventMapper() {
        return Mockito.mock(UsageEventMapper.class);
    }
}

