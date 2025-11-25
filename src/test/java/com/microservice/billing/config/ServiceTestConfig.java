package com.microservice.billing.config;

import com.microservice.billing.mapper.BillingRecordMapper;
import com.microservice.billing.mapper.UsageEventMapper;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

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

