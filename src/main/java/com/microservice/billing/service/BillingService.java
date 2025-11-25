package com.microservice.billing.service;

import com.microservice.billing.config.Features;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.BillingRecordRepository;
import com.microservice.billing.repository.UsageEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.togglz.core.manager.FeatureManager;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final UsageEventRepository usageEventRepository;
    private final BillingRecordRepository billingRecordRepository;
    private final FeatureManager featureManager;

    private static final BigDecimal RATE_PER_UNIT = new BigDecimal("0.01");

    @Transactional
    public BillingRecord calculateBilling(String customerId, LocalDate billingPeriod) {
        log.info("Calculating billing for customer: {}, period: {}", customerId, billingPeriod);
        return billingRecordRepository
                .findByCustomerIdAndBillingPeriod(customerId, billingPeriod)
                .orElseGet(() -> calculateAndSaveBilling(customerId, billingPeriod));
    }

    private BillingRecord calculateAndSaveBilling(String customerId, LocalDate billingPeriod) {
        if (!featureManager.isActive(Features.USAGE_AGGREGATION)) {
            throw new IllegalStateException("Usage aggregation is disabled");
        }

        LocalDateTime periodStart = billingPeriod.atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.plusMonths(1).atStartOfDay().minusSeconds(1);
        
        List<UsageEvent> usageEvents = usageEventRepository
                .findByCustomerIdAndDateRange(customerId, periodStart, periodEnd);

        BigDecimal totalAmount = usageEvents.stream()
                .map(UsageEvent::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(RATE_PER_UNIT);

        log.debug("Calculated billing: customer={}, period={}, events={}, total={}", 
                customerId, billingPeriod, usageEvents.size(), totalAmount);

        BillingRecord billingRecord = BillingRecord.builder()
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(totalAmount)
                .build();

        return billingRecordRepository.save(billingRecord);
    }

    public Optional<BillingRecord> getBillingRecord(String customerId, LocalDate billingPeriod) {
        return billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, billingPeriod);
    }

    public Map<String, BigDecimal> aggregateUsageByServiceType(String customerId) {
        return usageEventRepository.findByCustomerId(customerId).stream()
                .collect(Collectors.groupingBy(
                        UsageEvent::getServiceType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                UsageEvent::getQuantity,
                                BigDecimal::add
                        )
                ));
    }

    public BigDecimal getTotalUsageQuantity(String customerId) {
        return usageEventRepository.findByCustomerId(customerId).stream()
                .map(UsageEvent::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

