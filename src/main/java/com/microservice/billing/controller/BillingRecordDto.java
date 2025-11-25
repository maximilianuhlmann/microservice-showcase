package com.microservice.billing.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "Billing record data transfer object")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingRecordDto {

    @Schema(description = "Unique identifier of the billing record", example = "1")
    private Long id;

    @Schema(description = "Customer identifier", example = "customer-123")
    private String customerId;

    @Schema(description = "Billing period (first day of month)", example = "2024-01-01")
    private LocalDate billingPeriod;

    @Schema(description = "Total billing amount for the period", example = "10.50")
    private BigDecimal totalAmount;

    @Schema(description = "Timestamp when the billing record was created", example = "2024-02-01T00:00:00")
    private LocalDateTime createdAt;
}

