package com.microservice.billing.repository;

import com.microservice.billing.domain.ApiKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApiKeyRepository extends JpaRepository<ApiKey, Long> {
    Optional<ApiKey> findByKeyAndActiveTrue(String key);
    Optional<ApiKey> findByCustomerIdAndActiveTrue(String customerId);
}

