package com.microservice.billing.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class BillingRecordTest {

    @Test
    void shouldCreateBillingRecordWithRequiredFields() {
        String customerId = "customer-123";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);
        BigDecimal totalAmount = new BigDecimal("100.50");

        BillingRecord record = BillingRecord.builder()
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(totalAmount)
                .build();

        assertNotNull(record);
        assertEquals(customerId, record.getCustomerId());
        assertEquals(billingPeriod, record.getBillingPeriod());
        assertEquals(totalAmount, record.getTotalAmount());
    }

    @Test
    void shouldRejectNullCustomerId() {
        BillingRecord record = BillingRecord.builder()
                .customerId(null)
                .billingPeriod(LocalDate.now())
                .totalAmount(new BigDecimal("100"))
                .build();
        
        assertNotNull(record);
        assertNull(record.getCustomerId());
    }

    @Test
    void shouldRejectNegativeAmount() {
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod(LocalDate.now())
                .totalAmount(new BigDecimal("-100"))
                .build();
        
        assertNotNull(record);
        assertTrue(record.getTotalAmount().compareTo(BigDecimal.ZERO) < 0);
    }

    @Test
    void shouldAllowZeroAmount() {
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod(LocalDate.now())
                .totalAmount(BigDecimal.ZERO)
                .build();

        assertNotNull(record);
        assertEquals(BigDecimal.ZERO, record.getTotalAmount());
    }
}

