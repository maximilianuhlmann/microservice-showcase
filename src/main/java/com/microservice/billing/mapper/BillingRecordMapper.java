package com.microservice.billing.mapper;

import com.microservice.billing.controller.BillingRecordDto;
import com.microservice.billing.domain.BillingRecord;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillingRecordMapper {

    BillingRecordDto toDto(BillingRecord record);

    List<BillingRecordDto> toDtoList(List<BillingRecord> records);
}
