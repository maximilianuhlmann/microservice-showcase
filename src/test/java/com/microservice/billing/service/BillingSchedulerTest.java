package com.microservice.billing.service;

import com.microservice.billing.domain.Customer;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingSchedulerTest {

    @Mock
    private BillingService billingService;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private BillingScheduler scheduler;

    private Customer activeCustomer1;
    private Customer activeCustomer2;
    private Customer inactiveCustomer;

    @BeforeEach
    void setUp() {
        activeCustomer1 = Customer.builder()
                .customerId("customer-1")
                .name("Active Customer 1")
                .active(true)
                .build();

        activeCustomer2 = Customer.builder()
                .customerId("customer-2")
                .name("Active Customer 2")
                .active(true)
                .build();

        inactiveCustomer = Customer.builder()
                .customerId("customer-3")
                .name("Inactive Customer")
                .active(false)
                .build();
    }

    @Test
    void shouldCalculateBillingForAllActiveCustomers() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        when(customerRepository.findAll()).thenReturn(List.of(activeCustomer1, activeCustomer2, inactiveCustomer));

        BillingRecord record1 = BillingRecord.builder()
                .id(1L)
                .customerId("customer-1")
                .billingPeriod(previousMonth.toString())
                .totalAmount(new BigDecimal("10.00"))
                .build();

        BillingRecord record2 = BillingRecord.builder()
                .id(2L)
                .customerId("customer-2")
                .billingPeriod(previousMonth.toString())
                .totalAmount(new BigDecimal("20.00"))
                .build();

        when(billingService.calculateBilling("customer-1", previousMonth)).thenReturn(record1);
        when(billingService.calculateBilling("customer-2", previousMonth)).thenReturn(record2);

        scheduler.calculateMonthlyBilling();

        verify(customerRepository).findAll();
        verify(billingService).calculateBilling(eq("customer-1"), eq(previousMonth));
        verify(billingService).calculateBilling(eq("customer-2"), eq(previousMonth));
        verify(billingService, never()).calculateBilling(eq("customer-3"), any());
    }

    @Test
    void shouldHandleEmptyCustomerList() {
        when(customerRepository.findAll()).thenReturn(List.of());

        scheduler.calculateMonthlyBilling();

        verify(customerRepository).findAll();
        verify(billingService, never()).calculateBilling(anyString(), any());
    }

    @Test
    void shouldContinueOnErrorForOneCustomer() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        when(customerRepository.findAll()).thenReturn(List.of(activeCustomer1, activeCustomer2));

        when(billingService.calculateBilling("customer-1", previousMonth))
                .thenThrow(new RuntimeException("Database error"));
        when(billingService.calculateBilling("customer-2", previousMonth))
                .thenReturn(BillingRecord.builder()
                        .id(2L)
                        .customerId("customer-2")
                        .billingPeriod(previousMonth.toString())
                        .totalAmount(new BigDecimal("20.00"))
                        .build());

        scheduler.calculateMonthlyBilling();

        verify(billingService).calculateBilling("customer-1", previousMonth);
        verify(billingService).calculateBilling("customer-2", previousMonth);
    }

    @Test
    void shouldFilterOnlyActiveCustomers() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        when(customerRepository.findAll()).thenReturn(List.of(activeCustomer1, inactiveCustomer));

        when(billingService.calculateBilling("customer-1", previousMonth))
                .thenReturn(BillingRecord.builder()
                        .id(1L)
                        .customerId("customer-1")
                        .billingPeriod(previousMonth.toString())
                        .totalAmount(new BigDecimal("10.00"))
                        .build());

        scheduler.calculateMonthlyBilling();

        verify(billingService).calculateBilling(eq("customer-1"), eq(previousMonth));
        verify(billingService, never()).calculateBilling(eq("customer-3"), any());
    }
}

