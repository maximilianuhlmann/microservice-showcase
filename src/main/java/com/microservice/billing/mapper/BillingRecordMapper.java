package com.microservice.billing.mapper;

import com.microservice.billing.controller.BillingBreakdownDto;
import com.microservice.billing.controller.BillingRecordDto;
import com.microservice.billing.domain.BillingBreakdown;
import com.microservice.billing.domain.BillingRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface BillingRecordMapper {

    @Mapping(target = "breakdown", ignore = true)
    BillingRecordDto toDto(BillingRecord billingRecord);

    default BillingRecordDto toDto(BillingRecord billingRecord, List<BillingBreakdown> breakdown) {
        BillingRecordDto dto = toDto(billingRecord);
        if (dto != null && breakdown != null) {
            dto.setBreakdown(breakdown.stream()
                    .map(b -> BillingBreakdownDto.builder()
                            .serviceType(b.getServiceType())
                            .quantity(b.getQuantity())
                            .rate(b.getRate())
                            .amount(b.getAmount())
                            .build())
                    .toList());
        }
        return dto;
    }

    List<BillingRecordDto> toDtoList(List<BillingRecord> records);
}
