package com.microservice.billing.controller;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Usage event data transfer object")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsageEventDto {

    @Schema(description = "Unique identifier of the usage event", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "Customer identifier", example = "customer-123", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Customer ID is required")
    @jakarta.validation.constraints.Size(max = 255, message = "Customer ID must not exceed 255 characters")
    private String customerId;

    @Schema(description = "Type of service being used", example = "api-calls", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Service type is required")
    @jakarta.validation.constraints.Size(max = 255, message = "Service type must not exceed 255 characters")
    private String serviceType;

    @Schema(description = "Quantity of usage", example = "10.5", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private BigDecimal quantity;

    @Schema(description = "Unit of measurement", example = "requests")
    @NotBlank(message = "Unit is required")
    @jakarta.validation.constraints.Size(max = 50, message = "Unit must not exceed 50 characters")
    private String unit;

    @Schema(description = "Timestamp when the usage occurred. If not provided, defaults to current time.", example = "2024-01-15T10:30:00")
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime timestamp;

    @Schema(description = "Optional metadata as JSON string. Used for audit trail. Not used in billing calculations.", example = "{\"endpoint\":\"/api/users\",\"method\":\"GET\"}")
    @jakarta.validation.constraints.Size(max = 1000, message = "Metadata must not exceed 1000 characters")
    private String metadata;
}

