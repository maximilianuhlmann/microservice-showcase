package com.microservice.billing.repository;

import com.microservice.billing.domain.UsageEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UsageEventRepository extends JpaRepository<UsageEvent, UUID> {
    
    List<UsageEvent> findByCustomerId(String customerId);
    
    List<UsageEvent> findByCustomerIdAndServiceType(String customerId, String serviceType);
    
    @Query("SELECT u FROM UsageEvent u WHERE u.customerId = :customerId AND u.timestamp >= :startDate AND u.timestamp <= :endDate")
    List<UsageEvent> findByCustomerIdAndDateRange(
        @Param("customerId") String customerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}

