package com.microservice.billing.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    @Schema(description = "Billing period in YYYY-MM format", example = "2025-11")
    private String billingPeriod;

    @Schema(description = "Total billing amount for the period", example = "10.50")
    private BigDecimal totalAmount;

    @Schema(description = "Breakdown by service type", example = "[{\"serviceType\":\"api-calls\",\"quantity\":1000,\"rate\":0.001,\"amount\":1.00}]")
    private List<BillingBreakdownDto> breakdown;

    @Schema(description = "Timestamp when the billing record was created", example = "2024-02-01T00:00:00")
    private LocalDateTime createdAt;
}

