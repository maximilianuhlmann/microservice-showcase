package com.microservice.billing.service;

import com.microservice.billing.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.YearMonth;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "billing.scheduler.enabled", havingValue = "true", matchIfMissing = false)
public class BillingScheduler {

    private final BillingService billingService;
    private final CustomerRepository customerRepository;

    @Scheduled(cron = "${billing.scheduler.cron:0 0 1 * * ?}")
    public void calculateMonthlyBilling() {
        YearMonth previousMonth = YearMonth.now().minusMonths(1);
        log.info("Starting scheduled billing calculation for period: {}", previousMonth);
        
        customerRepository.findAll().stream()
                .filter(customer -> Boolean.TRUE.equals(customer.getActive()))
                .forEach(customer -> {
                    try {
                        log.debug("Calculating billing for customer: {}, period: {}", customer.getCustomerId(), previousMonth);
                        billingService.calculateBilling(customer.getCustomerId(), previousMonth);
                    } catch (Exception e) {
                        log.error("Failed to calculate billing for customer: {}, period: {}", 
                                customer.getCustomerId(), previousMonth, e);
                    }
                });
        
        log.info("Completed scheduled billing calculation for period: {}", previousMonth);
    }
}

