package com.microservice.billing.service;

import com.microservice.billing.config.Features;
import com.microservice.billing.domain.BillingBreakdown;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.BillingBreakdownRepository;
import com.microservice.billing.repository.BillingRecordRepository;
import com.microservice.billing.repository.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.togglz.core.manager.FeatureManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingServiceBreakdownTest {

    @Mock
    private UsageEventRepository usageEventRepository;

    @Mock
    private BillingRecordRepository billingRecordRepository;

    @Mock
    private BillingBreakdownRepository billingBreakdownRepository;

    @Mock
    private FeatureManager featureManager;

    @Mock
    private PricingService pricingService;

    @InjectMocks
    private BillingService billingService;

    @Test
    void shouldCreateBreakdownForMultipleServiceTypes() {
        when(featureManager.isActive(Features.USAGE_AGGREGATION)).thenReturn(true);
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);
        LocalDateTime periodStart = billingPeriod.atDay(1).atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.atEndOfMonth().atTime(23, 59, 59);

        UsageEvent apiEvent = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("api-calls")
                .quantity(new BigDecimal("1000"))
                .unit("requests")
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        UsageEvent storageEvent = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("storage")
                .quantity(new BigDecimal("50"))
                .unit("gb")
                .timestamp(LocalDateTime.of(2024, 1, 20, 10, 0))
                .build();

        when(usageEventRepository.findByCustomerIdAndDateRange(customerId, periodStart, periodEnd))
                .thenReturn(List.of(apiEvent, storageEvent));
        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, "2024-01"))
                .thenReturn(Optional.empty());
        when(billingRecordRepository.save(any(BillingRecord.class)))
                .thenAnswer(invocation -> {
                    BillingRecord billingRecord = invocation.getArgument(0);
                    return BillingRecord.builder()
                            .id(1L)
                            .customerId(billingRecord.getCustomerId())
                            .billingPeriod(billingRecord.getBillingPeriod())
                            .totalAmount(billingRecord.getTotalAmount())
                            .build();
                });
        when(pricingService.getRateForServiceType(customerId, "api-calls"))
                .thenReturn(new BigDecimal("0.001"));
        when(pricingService.getRateForServiceType(customerId, "storage"))
                .thenReturn(new BigDecimal("0.10"));
        when(pricingService.calculateCost(eq(customerId), eq("api-calls"), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("1.00"));
        when(pricingService.calculateCost(eq(customerId), eq("storage"), any(BigDecimal.class)))
                .thenReturn(new BigDecimal("5.00"));

        BillingRecord result = billingService.calculateBilling(customerId, billingPeriod);

        assertNotNull(result);
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<BillingBreakdown>> breakdownCaptor = ArgumentCaptor.forClass(List.class);
        verify(billingBreakdownRepository).saveAll(breakdownCaptor.capture());

        List<BillingBreakdown> breakdowns = breakdownCaptor.getValue();
        assertEquals(2, breakdowns.size());

        BillingBreakdown apiBreakdown = breakdowns.stream()
                .filter(b -> "api-calls".equals(b.getServiceType()))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("1000"), apiBreakdown.getQuantity());
        assertEquals(new BigDecimal("0.001"), apiBreakdown.getRate());
        assertEquals(new BigDecimal("1.00"), apiBreakdown.getAmount());

        BillingBreakdown storageBreakdown = breakdowns.stream()
                .filter(b -> "storage".equals(b.getServiceType()))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("50"), storageBreakdown.getQuantity());
        assertEquals(new BigDecimal("0.10"), storageBreakdown.getRate());
        assertEquals(new BigDecimal("5.00"), storageBreakdown.getAmount());
    }

    @Test
    void shouldRetrieveBreakdownForBillingRecord() {
        Long billingRecordId = 1L;
        BillingBreakdown breakdown = BillingBreakdown.builder()
                .id(1L)
                .serviceType("api-calls")
                .quantity(new BigDecimal("1000"))
                .rate(new BigDecimal("0.001"))
                .amount(new BigDecimal("1.00"))
                .build();

        when(billingBreakdownRepository.findByBillingRecordId(billingRecordId))
                .thenReturn(List.of(breakdown));

        List<BillingBreakdown> result = billingService.getBillingBreakdown(billingRecordId);

        assertEquals(1, result.size());
        assertEquals("api-calls", result.get(0).getServiceType());
        assertEquals(0, new BigDecimal("1.00").compareTo(result.get(0).getAmount()));
    }
}

