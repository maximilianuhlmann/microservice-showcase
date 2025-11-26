package com.microservice.billing.repository;

import com.microservice.billing.domain.DefaultRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DefaultRateRepository extends JpaRepository<DefaultRate, Long> {
    Optional<DefaultRate> findByServiceTypeAndActiveTrue(String serviceType);
}

