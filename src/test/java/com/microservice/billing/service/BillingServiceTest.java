package com.microservice.billing.service;

import com.microservice.billing.config.BillingServiceTestConfiguration;
import com.microservice.billing.config.Features;
import com.microservice.billing.config.PostgresTestContainer;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.exception.FeatureDisabledException;
import com.microservice.billing.repository.BillingBreakdownRepository;
import com.microservice.billing.repository.BillingRecordRepository;
import com.microservice.billing.repository.UsageEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
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

@SpringBootTest(
    classes = BillingServiceTestConfiguration.class,
    properties = "spring.main.allow-bean-definition-overriding=true"
)
@org.springframework.test.context.ContextConfiguration(initializers = PostgresTestContainer.class)
@TestPropertySource(properties = {
        "spring.flyway.enabled=true",
        "spring.jpa.hibernate.ddl-auto=none"
})
class BillingServiceTest {

    @MockBean
    private UsageEventRepository usageEventRepository;

    @MockBean
    private BillingRecordRepository billingRecordRepository;

    @MockBean
    private BillingBreakdownRepository billingBreakdownRepository;

    @MockBean
    private FeatureManager featureManager;

    @MockBean
    private com.microservice.billing.service.PricingService pricingService;

    @SpyBean
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        when(featureManager.isActive(Features.USAGE_AGGREGATION)).thenReturn(true);
    }

    @Test
    void shouldCalculateBillingForCustomer() {
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);
        LocalDateTime periodStart = billingPeriod.atDay(1).atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.atEndOfMonth().atTime(23, 59, 59);

        UsageEvent event1 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("api-calls")
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.of(2024, 1, 15, 10, 0))
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("api-calls")
                .quantity(new BigDecimal("20"))
                .unit("requests")
                .timestamp(LocalDateTime.of(2024, 1, 20, 10, 0))
                .build();

        when(usageEventRepository.findByCustomerIdAndDateRange(customerId, periodStart, periodEnd))
                .thenReturn(List.of(event1, event2));
        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, "2024-01"))
                .thenReturn(Optional.empty());
        when(billingRecordRepository.save(any(BillingRecord.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(billingBreakdownRepository.saveAll(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(pricingService.getRateForServiceType(customerId, "api-calls"))
                .thenReturn(new BigDecimal("0.001"));
        when(pricingService.calculateCost(eq(customerId), eq("api-calls"), any(BigDecimal.class)))
                .thenAnswer(invocation -> {
                    BigDecimal quantity = invocation.getArgument(2);
                    return quantity.multiply(new BigDecimal("0.001"));
                });

        BillingRecord result = billingService.calculateBilling(customerId, billingPeriod);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        assertEquals("2024-01", result.getBillingPeriod());
        assertEquals(new BigDecimal("0.030"), result.getTotalAmount());
        verify(billingRecordRepository, times(1)).save(any(BillingRecord.class));
    }

    @Test
    void shouldReturnExistingBillingRecordIfAlreadyCalculated() {
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);

        BillingRecord existing = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("100.50"))
                .build();

        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, "2024-01"))
                .thenReturn(Optional.of(existing));

        BillingRecord result = billingService.calculateBilling(customerId, billingPeriod);

        assertNotNull(result);
        assertEquals(existing.getId(), result.getId());
        assertEquals(new BigDecimal("100.50"), result.getTotalAmount());
        verify(billingRecordRepository, never()).save(any(BillingRecord.class));
    }

    @Test
    void shouldGetBillingRecordByCustomerAndPeriod() {
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);

        BillingRecord record = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("100.50"))
                .build();

        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, "2024-01"))
                .thenReturn(Optional.of(record));

        Optional<BillingRecord> result = billingService.getBillingRecord(customerId, billingPeriod);

        assertTrue(result.isPresent());
        assertEquals(customerId, result.get().getCustomerId());
    }

    @Test
    void shouldThrowFeatureDisabledExceptionWhenFeatureIsDisabled() {
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);

        when(featureManager.isActive(Features.USAGE_AGGREGATION)).thenReturn(false);
        when(billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, "2024-01"))
                .thenReturn(Optional.empty());

        FeatureDisabledException exception = assertThrows(FeatureDisabledException.class,
                () -> billingService.calculateBilling(customerId, billingPeriod));

        assertEquals("USAGE_AGGREGATION", exception.getFeatureName());
        assertTrue(exception.getMessage().contains("USAGE_AGGREGATION"));
    }
}

