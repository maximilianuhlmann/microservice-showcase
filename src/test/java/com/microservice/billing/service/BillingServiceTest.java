package com.microservice.billing.service;

import com.microservice.billing.config.BillingServiceTestConfiguration;
import com.microservice.billing.config.Features;
import com.microservice.billing.config.PostgresTestContainer;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.domain.UsageEvent;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
    private FeatureManager featureManager;

    // Mappers are provided by ServiceTestConfig as @Primary mocks
    // We can override them with @MockBean if needed for specific test behavior

    @SpyBean
    private BillingService billingService;

    @BeforeEach
    void setUp() {
        // Enable USAGE_AGGREGATION feature by default for tests
        when(featureManager.isActive(Features.USAGE_AGGREGATION)).thenReturn(true);
    }

    @Test
    void shouldCalculateBillingForCustomer() {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);
        LocalDateTime periodStart = billingPeriod.atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.plusMonths(1).atStartOfDay().minusSeconds(1);

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

