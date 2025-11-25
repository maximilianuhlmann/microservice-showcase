package com.microservice.billing.mapper;

import com.microservice.billing.controller.UsageEventDto;
import com.microservice.billing.domain.UsageEvent;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UsageEventMapper {

    UsageEventDto toDto(UsageEvent event);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    UsageEvent toEntity(UsageEventDto dto);

    List<UsageEventDto> toDtoList(List<UsageEvent> events);

    List<UsageEvent> toEntityList(List<UsageEventDto> dtos);
}
