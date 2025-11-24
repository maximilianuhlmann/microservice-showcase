package com.microservice.billing.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BillingRecordTest {

    @Test
    void shouldCreateBillingRecordWithRequiredFields() {
        // Given
        String customerId = "customer-123";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);
        BigDecimal totalAmount = new BigDecimal("100.50");

        // When
        BillingRecord record = BillingRecord.builder()
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(totalAmount)
                .build();

        // Then
        assertNotNull(record);
        assertEquals(customerId, record.getCustomerId());
        assertEquals(billingPeriod, record.getBillingPeriod());
        assertEquals(totalAmount, record.getTotalAmount());
    }

    @Test
    void shouldRejectNullCustomerId() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            BillingRecord.builder()
                    .customerId(null)
                    .billingPeriod(LocalDate.now())
                    .totalAmount(new BigDecimal("100"))
                    .build();
        });
    }

    @Test
    void shouldRejectNegativeAmount() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            BillingRecord.builder()
                    .customerId("customer-1")
                    .billingPeriod(LocalDate.now())
                    .totalAmount(new BigDecimal("-100"))
                    .build();
        });
    }

    @Test
    void shouldAllowZeroAmount() {
        // When
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod(LocalDate.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        // Then
        assertNotNull(record);
        assertEquals(BigDecimal.ZERO, record.getTotalAmount());
    }
}

