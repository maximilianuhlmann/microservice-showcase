package com.microservice.billing.controller;

import com.microservice.billing.mapper.UsageEventMapper;
import com.microservice.billing.service.UsageEventService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/usage-events")
@RequiredArgsConstructor
@Tag(name = "Usage Events", description = "API for recording and retrieving usage events for billing")
public class UsageEventController {

    private final UsageEventService usageEventService;
    private final UsageEventMapper usageEventMapper;

    @Operation(summary = "Record a usage event")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usage event created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<UsageEventDto> recordUsage(@Valid @RequestBody UsageEventDto dto) {
        log.info("Recording usage event for customer: {}, service: {}, quantity: {}", 
                dto.getCustomerId(), dto.getServiceType(), dto.getQuantity());
        
        var saved = usageEventService.recordUsage(usageEventMapper.toEntity(dto));
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(usageEventMapper.toDto(saved));
    }

    @Operation(summary = "Get usage events by customer")
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<UsageEventDto>> getUsageEventsByCustomer(
            @Parameter(description = "Customer identifier", required = true, example = "customer-123")
            @PathVariable String customerId) {
        log.info("Retrieving usage events for customer: {}", customerId);
        var events = usageEventService.getUsageEventsByCustomer(customerId);
        return ResponseEntity.ok(usageEventMapper.toDtoList(events));
    }

    @Operation(summary = "Get usage events by customer and service type")
    @GetMapping("/customer/{customerId}/service/{serviceType}")
    public ResponseEntity<List<UsageEventDto>> getUsageEventsByCustomerAndServiceType(
            @Parameter(description = "Customer identifier", required = true, example = "customer-123")
            @PathVariable String customerId,
            @Parameter(description = "Service type", required = true, example = "api-calls")
            @PathVariable String serviceType) {
        log.info("Retrieving usage events for customer: {} and service: {}", customerId, serviceType);
        var events = usageEventService.getUsageEventsByCustomerAndServiceType(customerId, serviceType);
        return ResponseEntity.ok(usageEventMapper.toDtoList(events));
    }
}

