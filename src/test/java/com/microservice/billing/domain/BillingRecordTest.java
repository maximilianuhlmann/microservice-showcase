package com.microservice.billing.domain;

import com.microservice.billing.exception.DomainValidationException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class BillingRecordTest {

    @Test
    void shouldCreateBillingRecordWithRequiredFields() {
        String customerId = "customer-123";
        String billingPeriod = "2024-01";
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
    void shouldRejectNullCustomerId() throws Exception {
        BillingRecord record = BillingRecord.builder()
                .customerId(null)
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("100"))
                .build();
        
        Exception exception = assertThrows(Exception.class,
                () -> {
                    java.lang.reflect.Method method = BillingRecord.class.getDeclaredMethod("onCreate");
                    method.setAccessible(true);
                    method.invoke(record);
                });
        
        DomainValidationException domainException = (DomainValidationException) exception.getCause();
        assertEquals("customerId", domainException.getFieldName());
        assertTrue(domainException.getMessage().contains("Customer ID cannot be null or blank"));
    }

    @Test
    void shouldRejectNegativeAmount() throws Exception {
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("-100"))
                .build();
        
        Exception exception = assertThrows(Exception.class,
                () -> {
                    java.lang.reflect.Method method = BillingRecord.class.getDeclaredMethod("onCreate");
                    method.setAccessible(true);
                    method.invoke(record);
                });
        
        DomainValidationException domainException = (DomainValidationException) exception.getCause();
        assertEquals("totalAmount", domainException.getFieldName());
        assertTrue(domainException.getMessage().contains("Total amount cannot be negative"));
    }

    @Test
    void shouldAllowZeroAmount() {
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod("2024-01")
                .totalAmount(BigDecimal.ZERO)
                .build();

        assertNotNull(record);
        assertEquals(BigDecimal.ZERO, record.getTotalAmount());
    }
}

