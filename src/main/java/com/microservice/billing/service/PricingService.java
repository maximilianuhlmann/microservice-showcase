package com.microservice.billing.service;

import com.microservice.billing.repository.DefaultRateRepository;
import com.microservice.billing.repository.PricingRateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
public class PricingService {

    private static final BigDecimal DEFAULT_RATE = new BigDecimal("0.01");
    
    private final PricingRateRepository pricingRateRepository;
    private final DefaultRateRepository defaultRateRepository;

    public PricingService(
            PricingRateRepository pricingRateRepository,
            DefaultRateRepository defaultRateRepository) {
        this.pricingRateRepository = pricingRateRepository;
        this.defaultRateRepository = defaultRateRepository;
    }

    public BigDecimal getRateForServiceType(String customerId, String serviceType) {
        if (customerId != null) {
            return pricingRateRepository
                    .findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType)
                    .map(com.microservice.billing.domain.PricingRate::getRate)
                    .orElseGet(() -> defaultRateRepository
                            .findByServiceTypeAndActiveTrue(serviceType)
                            .map(com.microservice.billing.domain.DefaultRate::getRate)
                            .orElse(DEFAULT_RATE));
        }
        
        return defaultRateRepository
                .findByServiceTypeAndActiveTrue(serviceType)
                .map(com.microservice.billing.domain.DefaultRate::getRate)
                .orElse(DEFAULT_RATE);
    }

    public BigDecimal calculateCost(String customerId, String serviceType, BigDecimal quantity) {
        BigDecimal rate = getRateForServiceType(customerId, serviceType);
        return quantity.multiply(rate);
    }
}

