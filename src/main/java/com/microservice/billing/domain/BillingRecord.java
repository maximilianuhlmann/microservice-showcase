package com.microservice.billing.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "billing_records", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"customer_id", "billing_period"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private String customerId;

    @Column(name = "billing_period", nullable = false, length = 7)
    private String billingPeriod;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        validate();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
        validate();
    }

    private void validate() {
        if (customerId == null || customerId.isBlank()) {
            throw new com.microservice.billing.exception.DomainValidationException("customerId", customerId, "Customer ID cannot be null or blank");
        }
        if (billingPeriod == null || billingPeriod.isBlank()) {
            throw new com.microservice.billing.exception.DomainValidationException("billingPeriod", billingPeriod, "Billing period cannot be null or blank");
        }
        if (totalAmount == null) {
            throw new com.microservice.billing.exception.DomainValidationException("totalAmount", totalAmount, "Total amount cannot be null");
        }
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            throw new com.microservice.billing.exception.DomainValidationException("totalAmount", totalAmount, "Total amount cannot be negative");
        }
    }
}

