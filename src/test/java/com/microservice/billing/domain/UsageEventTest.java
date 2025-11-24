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
        String serviceId = "service-api-calls";
        BigDecimal quantity = new BigDecimal("10.5");
        LocalDateTime timestamp = LocalDateTime.now();

        // When
        UsageEvent event = UsageEvent.builder()
                .customerId(customerId)
                .serviceId(serviceId)
                .quantity(quantity)
                .timestamp(timestamp)
                .build();

        // Then
        assertNotNull(event);
        assertEquals(customerId, event.getCustomerId());
        assertEquals(serviceId, event.getServiceId());
        assertEquals(quantity, event.getQuantity());
        assertEquals(timestamp, event.getTimestamp());
    }

    @Test
    void shouldRejectNullCustomerId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UsageEvent.builder()
                    .customerId(null)
                    .serviceId("service-1")
                    .quantity(new BigDecimal("1"))
                    .timestamp(LocalDateTime.now())
                    .build();
        });
    }

    @Test
    void shouldRejectNullServiceId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UsageEvent.builder()
                    .customerId("customer-1")
                    .serviceId(null)
                    .quantity(new BigDecimal("1"))
                    .timestamp(LocalDateTime.now())
                    .build();
        });
    }

    @Test
    void shouldRejectNegativeQuantity() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UsageEvent.builder()
                    .customerId("customer-1")
                    .serviceId("service-1")
                    .quantity(new BigDecimal("-1"))
                    .timestamp(LocalDateTime.now())
                    .build();
        });
    }

    @Test
    void shouldRejectNullQuantity() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            UsageEvent.builder()
                    .customerId("customer-1")
                    .serviceId("service-1")
                    .quantity(null)
                    .timestamp(LocalDateTime.now())
                    .build();
        });
    }
}

