package com.microservice.billing.mapper;

import com.microservice.billing.controller.BillingRecordDto;
import com.microservice.billing.domain.BillingRecord;
import org.mapstruct.Mapper;

import java.util.List;

/**
 * MapStruct mapper for transforming BillingRecord entities to DTOs.
 * MapStruct generates the implementation at compile time.
 */
@Mapper(componentModel = "spring")
public interface BillingRecordMapper {

    /**
     * Maps a BillingRecord entity to a BillingRecordDto.
     * 
     * @param record the entity to map
     * @return the DTO representation
     */
    BillingRecordDto toDto(BillingRecord record);

    /**
     * Maps a list of BillingRecord entities to a list of BillingRecordDto.
     * MapStruct automatically generates this from the single entity mapper.
     * 
     * @param records the list of entities to map
     * @return the list of DTOs
     */
    List<BillingRecordDto> toDtoList(List<BillingRecord> records);
}
