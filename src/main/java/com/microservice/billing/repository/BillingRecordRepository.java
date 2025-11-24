package com.microservice.billing.repository;

import com.microservice.billing.domain.BillingRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface BillingRecordRepository extends JpaRepository<BillingRecord, Long> {
    
    Optional<BillingRecord> findByCustomerIdAndBillingPeriod(String customerId, LocalDate billingPeriod);
}

