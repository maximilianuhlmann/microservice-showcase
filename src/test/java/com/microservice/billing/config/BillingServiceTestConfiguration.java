package com.microservice.billing.config;

import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Custom Spring Boot test configuration for BillingServiceTest.
 * 
 * This configuration:
 * - Enables auto-configuration (JPA, Flyway, etc.)
 * - Enables JPA repositories
 * - Scans app packages but excludes *MapperImpl classes, TogglzConfig, and Controllers
 * - Imports ServiceTestConfig for mock mappers
 */
@EnableAutoConfiguration(exclude = {})
@EnableJpaRepositories(basePackages = "com.microservice.billing.repository")
@EntityScan(basePackages = "com.microservice.billing.domain")
@ComponentScan(
    basePackages = {"com.microservice.billing.service", "com.microservice.billing.repository", "com.microservice.billing.domain", "com.microservice.billing.config"},
    excludeFilters = {
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*MapperImpl"
        ),
        @ComponentScan.Filter(
            type = FilterType.ASSIGNABLE_TYPE,
            classes = com.microservice.billing.config.TogglzConfig.class
        ),
        @ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = ".*Controller"
        )
    }
)
@Import({ServiceTestConfig.class})
public class BillingServiceTestConfiguration {
}

