package com.microservice.billing.repository;

import com.microservice.billing.domain.PricingRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PricingRateRepository extends JpaRepository<PricingRate, Long> {
    Optional<PricingRate> findByCustomerIdAndServiceTypeAndActiveTrue(String customerId, String serviceType);
    List<PricingRate> findByCustomerIdAndActiveTrue(String customerId);
}

