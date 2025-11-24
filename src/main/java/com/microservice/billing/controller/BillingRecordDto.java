package com.microservice.billing.controller;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRecordDto {

    private Long id;
    private String customerId;
    private LocalDate billingPeriod;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}

