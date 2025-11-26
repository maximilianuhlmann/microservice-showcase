package com.microservice.billing.service;

import com.microservice.billing.domain.DefaultRate;
import com.microservice.billing.domain.PricingRate;
import com.microservice.billing.repository.DefaultRateRepository;
import com.microservice.billing.repository.PricingRateRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PricingServiceTest {

    @Mock
    private PricingRateRepository pricingRateRepository;

    @Mock
    private DefaultRateRepository defaultRateRepository;

    @InjectMocks
    private PricingService pricingService;

    @Test
    void shouldUseCustomerSpecificRateWhenAvailable() {
        String customerId = "customer-1";
        String serviceType = "api-calls";
        BigDecimal customerRate = new BigDecimal("0.002");

        PricingRate pricingRate = PricingRate.builder()
                .customerId(customerId)
                .serviceType(serviceType)
                .rate(customerRate)
                .active(true)
                .build();

        when(pricingRateRepository.findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType))
                .thenReturn(Optional.of(pricingRate));

        BigDecimal result = pricingService.getRateForServiceType(customerId, serviceType);

        assertEquals(customerRate, result);
        verify(pricingRateRepository).findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType);
        verify(defaultRateRepository, never()).findByServiceTypeAndActiveTrue(anyString());
    }

    @Test
    void shouldFallbackToDefaultRateWhenNoCustomerSpecificRate() {
        String customerId = "customer-1";
        String serviceType = "api-calls";
        BigDecimal defaultRate = new BigDecimal("0.001");

        DefaultRate defaultRateEntity = DefaultRate.builder()
                .serviceType(serviceType)
                .rate(defaultRate)
                .active(true)
                .build();

        when(pricingRateRepository.findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType))
                .thenReturn(Optional.empty());
        when(defaultRateRepository.findByServiceTypeAndActiveTrue(serviceType))
                .thenReturn(Optional.of(defaultRateEntity));

        BigDecimal result = pricingService.getRateForServiceType(customerId, serviceType);

        assertEquals(defaultRate, result);
        verify(pricingRateRepository).findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType);
        verify(defaultRateRepository).findByServiceTypeAndActiveTrue(serviceType);
    }

    @Test
    void shouldUseHardcodedDefaultWhenNoRatesInDatabase() {
        String customerId = "customer-1";
        String serviceType = "unknown-service";

        when(pricingRateRepository.findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType))
                .thenReturn(Optional.empty());
        when(defaultRateRepository.findByServiceTypeAndActiveTrue(serviceType))
                .thenReturn(Optional.empty());

        BigDecimal result = pricingService.getRateForServiceType(customerId, serviceType);

        assertEquals(new BigDecimal("0.01"), result);
    }

    @Test
    void shouldCalculateCostCorrectly() {
        String customerId = "customer-1";
        String serviceType = "api-calls";
        BigDecimal quantity = new BigDecimal("1000");
        BigDecimal rate = new BigDecimal("0.001");

        when(pricingRateRepository.findByCustomerIdAndServiceTypeAndActiveTrue(customerId, serviceType))
                .thenReturn(Optional.empty());
        when(defaultRateRepository.findByServiceTypeAndActiveTrue(serviceType))
                .thenReturn(Optional.of(DefaultRate.builder().rate(rate).build()));

        BigDecimal cost = pricingService.calculateCost(customerId, serviceType, quantity);

        assertEquals(0, new BigDecimal("1.00").compareTo(cost));
    }

    @Test
    void shouldHandleNullCustomerId() {
        String serviceType = "api-calls";
        BigDecimal defaultRate = new BigDecimal("0.001");

        DefaultRate defaultRateEntity = DefaultRate.builder()
                .serviceType(serviceType)
                .rate(defaultRate)
                .active(true)
                .build();

        when(defaultRateRepository.findByServiceTypeAndActiveTrue(serviceType))
                .thenReturn(Optional.of(defaultRateEntity));

        BigDecimal result = pricingService.getRateForServiceType(null, serviceType);

        assertEquals(defaultRate, result);
        verify(pricingRateRepository, never()).findByCustomerIdAndServiceTypeAndActiveTrue(anyString(), anyString());
    }
}

