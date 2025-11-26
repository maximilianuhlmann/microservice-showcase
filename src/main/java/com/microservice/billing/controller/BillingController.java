package com.microservice.billing.controller;

import com.microservice.billing.mapper.BillingRecordMapper;
import com.microservice.billing.service.BillingService;
import com.microservice.billing.service.CustomerContextService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/billing")
@RequiredArgsConstructor
@Tag(name = "Billing", description = "API for calculating and retrieving billing records")
public class BillingController {

    private final BillingService billingService;
    private final BillingRecordMapper billingRecordMapper;
    private final CustomerContextService customerContextService;

    @Operation(summary = "Calculate billing for a customer")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Billing calculated successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping("/{customerId}/calculate")
    public ResponseEntity<BillingRecordDto> calculateBilling(
            @Parameter(description = "Customer identifier", required = true, example = "customer-123")
            @PathVariable String customerId,
            @Parameter(description = "Billing period in YYYY-MM format", required = true, example = "2025-11")
            @RequestParam String billingPeriod) {
        
        customerContextService.verifyCustomerAccess(customerId);
        
        YearMonth period = YearMonth.parse(billingPeriod);
        log.info("Calculating billing for customer: {}, period: {}", customerId, period);
        var billingRecord = billingService.calculateBilling(customerId, period);
        return ResponseEntity.ok(billingRecordMapper.toDto(billingRecord, billingService.getBillingBreakdown(billingRecord.getId())));
    }

    @Operation(
        summary = "Get billing record",
        description = "Retrieves an existing billing record for a customer and period. " +
                     "Note: The billing record must be calculated first using POST /calculate. " +
                     "If the record shows totalAmount=0, it means no usage events were found for that period."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Billing record found"),
        @ApiResponse(responseCode = "404", description = "Billing record not found. Calculate billing first using POST /calculate.")
    })
    @GetMapping("/{customerId}")
    public ResponseEntity<BillingRecordDto> getBillingRecord(
            @Parameter(description = "Customer identifier", required = true, example = "customer-123")
            @PathVariable String customerId,
            @Parameter(description = "Billing period in YYYY-MM format", required = true, example = "2025-11")
            @RequestParam String billingPeriod) {
        
        customerContextService.verifyCustomerAccess(customerId);
        
        YearMonth period = YearMonth.parse(billingPeriod);
        log.info("Retrieving billing record for customer: {}, period: {}", customerId, period);
        return billingService.getBillingRecord(customerId, period)
                .map(billingRecord -> billingRecordMapper.toDto(billingRecord, billingService.getBillingBreakdown(billingRecord.getId())))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Operation(summary = "Get usage aggregation by service type")
    @GetMapping("/{customerId}/usage-by-service")
    public ResponseEntity<Map<String, BigDecimal>> getUsageByServiceType(
            @Parameter(description = "Customer identifier", required = true, example = "customer-123")
            @PathVariable String customerId) {
        
        customerContextService.verifyCustomerAccess(customerId);
        
        log.info("Aggregating usage by service type for customer: {}", customerId);
        Map<String, BigDecimal> aggregation = billingService.aggregateUsageByServiceType(customerId);
        return ResponseEntity.ok(aggregation);
    }

}

