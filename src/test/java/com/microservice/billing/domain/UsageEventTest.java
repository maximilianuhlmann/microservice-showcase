package com.microservice.billing.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UsageEventTest {

    @Test
    void shouldCreateUsageEventWithRequiredFields() {
        // Given
        String customerId = "customer-123";
        String serviceType = "api-calls";
        BigDecimal quantity = new BigDecimal("10.5");
        String unit = "requests";
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        UsageEvent event = UsageEvent.builder()
                .customerId(customerId)
                .serviceType(serviceType)
                .quantity(quantity)
                .unit(unit)
                .timestamp(timestamp)
                .build();

        // Then
        assertNotNull(event);
        assertEquals(customerId, event.getCustomerId());
        assertEquals(serviceType, event.getServiceType());
        assertEquals(quantity, event.getQuantity());
        assertEquals(unit, event.getUnit());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void shouldAllowNullCustomerIdInBuilder() {
        // When - Builder allows null (Lombok doesn't validate)
        UsageEvent event = UsageEvent.builder()
                .customerId(null)
                .serviceType("api-calls")
                .quantity(new BigDecimal("1"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();
        
        // Then - Builder allows null, validation happens at DTO level or persistence
        assertNotNull(event);
        assertNull(event.getCustomerId());
    }

    @Test
    void shouldAllowNullServiceTypeInBuilder() {
        // When - Builder allows null (Lombok doesn't validate)
        UsageEvent event = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType(null)
                .quantity(new BigDecimal("1"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();
        
        // Then - Builder allows null, validation happens at DTO level
        assertNotNull(event);
        assertNull(event.getServiceType());
    }

    @Test
    void shouldAllowNegativeQuantityInBuilder() {
        // When - Builder allows negative (validation happens at DTO level)
        UsageEvent event = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("-1"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();
        
        // Then - Builder allows negative, but DTO validation will catch it
        assertNotNull(event);
        assertTrue(event.getQuantity().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void shouldAllowNullQuantityInBuilder() {
        // When - Builder allows null (Lombok doesn't validate)
        UsageEvent event = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(null)
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();
        
        // Then - Builder allows null, validation happens at DTO level
        assertNotNull(event);
        assertNull(event.getQuantity());
    }
}

