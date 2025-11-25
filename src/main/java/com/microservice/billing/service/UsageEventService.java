package com.microservice.billing.service;

import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.UsageEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsageEventService {

    private final UsageEventRepository repository;

    @Transactional
    public UsageEvent recordUsage(UsageEvent event) {
        log.debug("Recording usage event for customer: {}, service: {}, quantity: {}", 
                event.getCustomerId(), event.getServiceType(), event.getQuantity());
        return repository.save(event);
    }

    public List<UsageEvent> getUsageEventsByCustomer(String customerId) {
        log.debug("Retrieving usage events for customer: {}", customerId);
        return repository.findByCustomerId(customerId);
    }

    public List<UsageEvent> getUsageEventsByCustomerAndServiceType(String customerId, String serviceType) {
        log.debug("Retrieving usage events for customer: {} and service: {}", customerId, serviceType);
        return repository.findByCustomerIdAndServiceType(customerId, serviceType);
    }
}

