package com.microservice.billing.mapper;

import com.microservice.billing.controller.UsageEventDto;
import com.microservice.billing.domain.UsageEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for transforming UsageEvent entities to DTOs and vice versa.
 * Follows the explicit mapper pattern for clear separation of concerns.
 */
@Component
@RequiredArgsConstructor
public class UsageEventMapper {

    /**
     * Maps a UsageEvent entity to a UsageEventDto.
     * 
     * @param event the entity to map
     * @return the DTO representation
     */
    public UsageEventDto mapToDto(UsageEvent event) {
        if (event == null) {
            return null;
        }
        
        return UsageEventDto.builder()
                .id(event.getId())
                .customerId(event.getCustomerId())
                .serviceType(event.getServiceType())
                .quantity(event.getQuantity())
                .unit(event.getUnit())
                .timestamp(event.getTimestamp())
                .metadata(event.getMetadata())
                .build();
    }

    /**
     * Maps a UsageEventDto to a UsageEvent entity.
     * 
     * @param dto the DTO to map
     * @return the entity representation
     */
    public UsageEvent mapToEntity(UsageEventDto dto) {
        if (dto == null) {
            return null;
        }
        
        return UsageEvent.builder()
                .customerId(dto.getCustomerId())
                .serviceType(dto.getServiceType())
                .quantity(dto.getQuantity())
                .unit(dto.getUnit())
                .timestamp(dto.getTimestamp())
                .metadata(dto.getMetadata())
                .build();
    }

    /**
     * Maps a list of UsageEvent entities to a list of UsageEventDto.
     * Demonstrates proficiency with streams and map method.
     * 
     * @param events the list of entities to map
     * @return the list of DTOs
     */
    public List<UsageEventDto> mapToDtoList(List<UsageEvent> events) {
        if (events == null || events.isEmpty()) {
            return List.of();
        }
        
        return events.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps a list of UsageEventDto to a list of UsageEvent entities.
     * 
     * @param dtos the list of DTOs to map
     * @return the list of entities
     */
    public List<UsageEvent> mapToEntityList(List<UsageEventDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return List.of();
        }
        
        return dtos.stream()
                .map(this::mapToEntity)
                .collect(Collectors.toList());
    }
}

