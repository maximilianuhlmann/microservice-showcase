package com.microservice.billing.service;

import com.microservice.billing.config.FeatureFlags;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.BillingRecordRepository;
import com.microservice.billing.repository.UsageEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceTest {

    @Mock
    private UsageEventRepository usageEventRepository;

    @Mock
    private BillingRecordRepository billingRecordRepository;

    @Mock
    private FeatureFlags featureFlags;

    @InjectMocks
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        when(featureFlags.isUsageAggregationEnabled()).thenReturn(true);
    }

    @Test
    void shouldCalculateBillingForCustomer() {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);

        UsageEvent event1 = UsageEvent.builder()
                .id(1L)
                .customerId(customerId)
                .serviceId("service-1")
                .quantity(new BigDecimal("10"))
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .id(2L)
                .customerId(customerId)
                .serviceId("service-1")
                .quantity(new BigDecimal("20"))
                .timestamp(LocalDateTime.of(2024, 1, 20, 10, 0))
                .build();

        when(usageEventRepository.findByCustomerId(customerId))
                .thenReturn(List.of(event1, event2));
        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, billingPeriod))
                .thenReturn(Optional.empty());
        when(billingRecordRepository.save(any(BillingRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When
        BillingRecord result = billingService.calculateBilling(customerId, billingPeriod);

        // Then
        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals(billingPeriod, result.getBillingPeriod());
        // Simple calculation: sum of quantities * 0.01 (mock rate)
        assertEquals(new BigDecimal("0.30"), result.getTotalAmount());
        verify(billingRecordRepository, times(1)).save(any(BillingRecord.class));
    }

    @Test
    void shouldReturnExistingBillingRecordIfAlreadyCalculated() {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);

        BillingRecord existing = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(new BigDecimal("100.50"))
                .build();

        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, billingPeriod))
                .thenReturn(Optional.of(existing));

        // When
        BillingRecord result = billingService.calculateBilling(customerId, billingPeriod);

        // Then
        assertNotNull(result);
        assertEquals(existing.getId(), result.getId());
        assertEquals(new BigDecimal("100.50"), result.getTotalAmount());
        verify(billingRecordRepository, never()).save(any(BillingRecord.class));
    }

    @Test
    void shouldGetBillingRecordByCustomerAndPeriod() {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);

        BillingRecord record = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(new BigDecimal("100.50"))
                .build();

        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, billingPeriod))
                .thenReturn(Optional.of(record));

        // When
        Optional<BillingRecord> result = billingService.getBillingRecord(customerId, billingPeriod);

        // Then
        assertTrue(result.isPresent());
        assertEquals(customerId, result.get().getCustomerId());
    }
}

