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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for calculating and managing billing records.
 * Demonstrates proficiency with streams, map, and aggregation operations.
 * Uses feature flags to control functionality.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillingService {

    private final UsageEventRepository usageEventRepository;
    private final BillingRecordRepository billingRecordRepository;

    // Simple rate per unit - in real implementation, this would come from a pricing service
    private static final BigDecimal RATE_PER_UNIT = new BigDecimal("0.01");

    /**
     * Calculates billing for a customer for a specific billing period.
     * Aggregates all usage events and applies pricing.
     * 
     * @param customerId the customer identifier
     * @param billingPeriod the billing period (typically first day of month)
     * @return the calculated billing record
     */
    @Transactional
    public BillingRecord calculateBilling(String customerId, LocalDate billingPeriod) {
        log.info("Calculating billing for customer: {}, period: {}", customerId, billingPeriod);
        
        // Check if billing already calculated
        return billingRecordRepository
                .findByCustomerIdAndBillingPeriod(customerId, billingPeriod)
                .orElseGet(() -> calculateAndSaveBilling(customerId, billingPeriod));
    }

    private BillingRecord calculateAndSaveBilling(String customerId, LocalDate billingPeriod) {
        if (!Features.USAGE_AGGREGATION.isActive()) {
            throw new IllegalStateException("Usage aggregation is disabled");
        }

        // Get all usage events for the customer within the billing period
        LocalDateTime periodStart = billingPeriod.atStartOfDay();
        LocalDateTime periodEnd = billingPeriod.plusMonths(1).atStartOfDay().minusSeconds(1);
        
        List<UsageEvent> usageEvents = usageEventRepository
                .findByCustomerIdAndDateRange(customerId, periodStart, periodEnd);

        // Calculate total amount using streams - demonstrates map and reduce
        BigDecimal totalAmount = usageEvents.stream()
                .map(UsageEvent::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .multiply(RATE_PER_UNIT);

        log.debug("Calculated billing: customer={}, period={}, events={}, total={}", 
                customerId, billingPeriod, usageEvents.size(), totalAmount);

        // Create and save billing record
        BillingRecord billingRecord = BillingRecord.builder()
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(totalAmount)
                .build();

        return billingRecordRepository.save(billingRecord);
    }

    /**
     * Gets billing record for a customer and period.
     * 
     * @param customerId the customer identifier
     * @param billingPeriod the billing period
     * @return optional billing record
     */
    public Optional<BillingRecord> getBillingRecord(String customerId, LocalDate billingPeriod) {
        return billingRecordRepository.findByCustomerIdAndBillingPeriod(customerId, billingPeriod);
    }

    /**
     * Aggregates usage by service type for a customer.
     * Demonstrates advanced stream operations with grouping.
     * 
     * @param customerId the customer identifier
     * @return map of service type to total quantity
     */
    public Map<String, BigDecimal> aggregateUsageByServiceType(String customerId) {
        List<UsageEvent> events = usageEventRepository.findByCustomerId(customerId);
        
        return events.stream()
                .collect(Collectors.groupingBy(
                        UsageEvent::getServiceType,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                UsageEvent::getQuantity,
                                BigDecimal::add
                        )
                ));
    }

    /**
     * Gets total usage quantity for a customer across all service types.
     * 
     * @param customerId the customer identifier
     * @return total quantity
     */
    public BigDecimal getTotalUsageQuantity(String customerId) {
        return usageEventRepository.findByCustomerId(customerId).stream()
                .map(UsageEvent::getQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}

