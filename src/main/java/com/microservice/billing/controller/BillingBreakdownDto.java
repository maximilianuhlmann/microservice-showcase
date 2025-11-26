package com.microservice.billing.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "Billing breakdown per service type")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillingBreakdownDto {

    @Schema(description = "Service type", example = "api-calls")
    private String serviceType;

    @Schema(description = "Total quantity used", example = "1000.00")
    private BigDecimal quantity;

    @Schema(description = "Rate per unit", example = "0.001")
    private BigDecimal rate;

    @Schema(description = "Total amount for this service type", example = "1.00")
    private BigDecimal amount;
}

