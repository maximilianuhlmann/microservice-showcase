package com.microservice.billing.service;

import com.microservice.billing.config.Features;
import com.microservice.billing.domain.BillingBreakdown;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.exception.FeatureDisabledException;
import com.microservice.billing.repository.BillingBreakdownRepository;
import com.microservice.billing.repository.BillingRecordRepository;
import com.microservice.billing.repository.UsageEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.togglz.core.manager.FeatureManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
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
    private final BillingBreakdownRepository billingBreakdownRepository;
    private final FeatureManager featureManager;
    private final PricingService pricingService;

    @Transactional
    public BillingRecord calculateBilling(String customerId, YearMonth billingPeriod) {
        String periodStr = billingPeriod.toString();
        log.info("Calculating billing for customer: {}, period: {}", customerId, periodStr);
        return billingRecordRepository
                .findByCustomerIdAndBillingPeriod(customerId, periodStr)
                .orElseGet(() -> calculateAndSaveBilling(customerId, billingPeriod));
    }

    private BillingRecord calculateAndSaveBilling(String customerId, YearMonth billingPeriod) {
        if (!featureManager.isActive(Features.USAGE_AGGREGATION)) {
            throw new FeatureDisabledException("USAGE_AGGREGATION");
        }

        LocalDateTime periodStart = billingPeriod.atDay(1).atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.atEndOfMonth().atTime(23, 59, 59);
        
        List<UsageEvent> usageEvents = usageEventRepository
                .findByCustomerIdAndDateRange(customerId, periodStart, periodEnd);

        Map<String, BillingBreakdownData> breakdownMap = usageEvents.stream()
                .collect(Collectors.groupingBy(
                        UsageEvent::getServiceType,
                        Collectors.collectingAndThen(
                                Collectors.toList(),
                                events -> {
                                    BigDecimal totalQuantity = events.stream()
                                            .map(UsageEvent::getQuantity)
                                            .reduce(BigDecimal.ZERO, BigDecimal::add);
                                    BigDecimal rate = pricingService.getRateForServiceType(customerId, events.get(0).getServiceType());
                                    BigDecimal amount = pricingService.calculateCost(customerId, events.get(0).getServiceType(), totalQuantity);
                                    return new BillingBreakdownData(events.get(0).getServiceType(), totalQuantity, rate, amount);
                                }
                        )
                ));

        BigDecimal totalAmount = breakdownMap.values().stream()
                .map(BillingBreakdownData::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String periodStr = billingPeriod.toString();
        log.info("Calculated billing: customer={}, period={} ({} to {}), events={}, total={}", 
                customerId, periodStr, periodStart, periodEnd, usageEvents.size(), totalAmount);

        BillingRecord billingRecord = BillingRecord.builder()
                .customerId(customerId)
                .billingPeriod(periodStr)
                .totalAmount(totalAmount)
                .build();

        final BillingRecord savedRecord = billingRecordRepository.save(billingRecord);

        List<BillingBreakdown> breakdowns = breakdownMap.values().stream()
                .map(data -> BillingBreakdown.builder()
                        .billingRecord(savedRecord)
                        .serviceType(data.serviceType())
                        .quantity(data.quantity())
                        .rate(data.rate())
                        .amount(data.amount())
                        .build())
                .toList();

        billingBreakdownRepository.saveAll(breakdowns);

        return savedRecord;
    }

    public Optional<BillingRecord> getBillingRecord(String customerId, YearMonth billingPeriod) {
        return billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, billingPeriod.toString());
    }

    public List<BillingBreakdown> getBillingBreakdown(Long billingRecordId) {
        return billingBreakdownRepository.findByBillingRecordId(billingRecordId);
    }

    private record BillingBreakdownData(String serviceType, BigDecimal quantity, BigDecimal rate, BigDecimal amount) {}

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

}

