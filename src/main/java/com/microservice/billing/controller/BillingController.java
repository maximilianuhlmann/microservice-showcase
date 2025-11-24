package com.microservice.billing.controller;

import com.microservice.billing.mapper.BillingRecordMapper;
import com.microservice.billing.service.BillingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * REST Controller for billing operations.
 * Provides endpoints for calculating and retrieving billing records.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "API for calculating and retrieving billing records")
public class BillingController {

    private final BillingService billingService;
    private final BillingRecordMapper billingRecordMapper;

    @Operation(
        summary = "Calculate billing for a customer",
        description = "Calculates billing for a customer for a specific billing period. " +
                     "Aggregates all usage events and applies pricing. " +
                     "If billing already exists, returns the existing record."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Billing calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/{customerId}/calculate")
    public ResponseEntity<BillingRecordDto> calculateBilling(
            @Parameter(description = "Customer identifier", required = true, example = "customer-123")
            @PathVariable String customerId,
            @Parameter(description = "Billing period (first day of month)", required = true, example = "2024-01-01")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingPeriod) {
        
        log.info("Calculating billing for customer: {}, period: {}", customerId, billingPeriod);
        var record = billingService.calculateBilling(customerId, billingPeriod);
        return ResponseEntity.ok(billingRecordMapper.toDto(record));
    }

    @Operation(
        summary = "Get billing record",
        description = "Retrieves an existing billing record for a customer and period."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Billing record found"),
        @ApiResponse(responseCode = "404", description = "Billing record not found")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<BillingRecordDto> getBillingRecord(
            @Parameter(description = "Customer identifier", required = true)
            @PathVariable String customerId,
            @Parameter(description = "Billing period", required = true)
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate billingPeriod) {
        
        log.info("Retrieving billing record for customer: {}, period: {}", customerId, billingPeriod);
        return billingService.getBillingRecord(customerId, billingPeriod)
                .map(billingRecordMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(
        summary = "Get usage aggregation by service type",
        description = "Returns aggregated usage quantities grouped by service type. " +
                     "Demonstrates advanced stream operations with grouping."
    )
    @GetMapping("/{customerId}/usage-by-service")
    public ResponseEntity<Map<String, BigDecimal>> getUsageByServiceType(
            @Parameter(description = "Customer identifier", required = true)
            @PathVariable String customerId) {
        
        log.info("Aggregating usage by service type for customer: {}", customerId);
        Map<String, BigDecimal> aggregation = billingService.aggregateUsageByServiceType(customerId);
        return ResponseEntity.ok(aggregation);
    }

    @Operation(
        summary = "Get total usage quantity",
        description = "Returns the total usage quantity across all service types for a customer."
    )
    @GetMapping("/{customerId}/total-usage")
    public ResponseEntity<BigDecimal> getTotalUsage(
            @Parameter(description = "Customer identifier", required = true)
            @PathVariable String customerId) {
        
        log.info("Getting total usage for customer: {}", customerId);
        BigDecimal total = billingService.getTotalUsageQuantity(customerId);
        return ResponseEntity.ok(total);
    }
}

