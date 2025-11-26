package com.microservice.billing.repository;

import com.microservice.billing.domain.BillingBreakdown;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BillingBreakdownRepository extends JpaRepository<BillingBreakdown, Long> {
    List<BillingBreakdown> findByBillingRecordId(Long billingRecordId);
}

