package com.microservice.billing.mapper;

import com.microservice.billing.controller.BillingRecordDto;
import com.microservice.billing.domain.BillingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Mapper for transforming BillingRecord entities to DTOs.
 */
@Component
@RequiredArgsConstructor
public class BillingRecordMapper {

    /**
     * Maps a BillingRecord entity to a BillingRecordDto.
     * 
     * @param record the entity to map
     * @return the DTO representation
     */
    public BillingRecordDto mapToDto(BillingRecord record) {
        if (record == null) {
            return null;
        }
        
        return BillingRecordDto.builder()
                .id(record.getId())
                .customerId(record.getCustomerId())
                .billingPeriod(record.getBillingPeriod())
                .totalAmount(record.getTotalAmount())
                .createdAt(record.getCreatedAt())
                .build();
    }

    /**
     * Maps an Optional BillingRecord to an Optional BillingRecordDto.
     * Demonstrates use of Optional with map method.
     * 
     * @param record the optional entity to map
     * @return the optional DTO
     */
    public Optional<BillingRecordDto> mapToDto(Optional<BillingRecord> record) {
        return record.map(this::mapToDto);
    }

    /**
     * Maps a list of BillingRecord entities to a list of BillingRecordDto.
     * 
     * @param records the list of entities to map
     * @return the list of DTOs
     */
    public List<BillingRecordDto> mapToDtoList(List<BillingRecord> records) {
        if (records == null || records.isEmpty()) {
            return List.of();
        }
        
        return records.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
}

