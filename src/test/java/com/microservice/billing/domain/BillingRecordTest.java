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

        BillingRecord billingRecord = BillingRecord.builder()
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(totalAmount)
                .build();

        assertNotNull(billingRecord);
        assertEquals(customerId, billingRecord.getCustomerId());
        assertEquals(billingPeriod, billingRecord.getBillingPeriod());
        assertEquals(totalAmount, billingRecord.getTotalAmount());
    }

    @Test
    void shouldRejectNullCustomerId() {
        BillingRecord billingRecord = BillingRecord.builder()
                .customerId(null)
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("100"))
                .build();
        
        Exception exception = assertThrows(Exception.class, () -> invokeOnCreate(billingRecord));
        
        DomainValidationException domainException = (DomainValidationException) exception.getCause();
        assertEquals("customerId", domainException.getFieldName());
        assertTrue(domainException.getMessage().contains("Customer ID cannot be null or blank"));
    }

    private void invokeOnCreate(BillingRecord billingRecord) throws Exception {
        java.lang.reflect.Method method = BillingRecord.class.getDeclaredMethod("onCreate");
        method.setAccessible(true);
        method.invoke(billingRecord);
    }

    @Test
    void shouldRejectNegativeAmount() {
        BillingRecord billingRecord = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("-100"))
                .build();
        
        Exception exception = assertThrows(Exception.class, () -> invokeOnCreate(billingRecord));
        
        DomainValidationException domainException = (DomainValidationException) exception.getCause();
        assertEquals("totalAmount", domainException.getFieldName());
        assertTrue(domainException.getMessage().contains("Total amount cannot be negative"));
    }

    @Test
    void shouldAllowZeroAmount() {
        BillingRecord billingRecord = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod("2024-01")
                .totalAmount(BigDecimal.ZERO)
                .build();

        assertNotNull(billingRecord);
        assertEquals(BigDecimal.ZERO, billingRecord.getTotalAmount());
    }
}

