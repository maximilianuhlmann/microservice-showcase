package com.microservice.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.mapper.UsageEventMapper;
import com.microservice.billing.service.UsageEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UsageEventController.class, excludeAutoConfiguration = {
        org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class UsageEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsageEventService usageEventService;

    @MockBean
    private UsageEventMapper usageEventMapper;

    @MockBean
    private com.microservice.billing.service.CustomerContextService customerContextService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldGetUsageEventsByCustomer() throws Exception {
        String customerId = "customer-1";
        UsageEvent event1 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("api-calls")
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("storage")
                .quantity(new BigDecimal("20"))
                .unit("gb")
                .timestamp(LocalDateTime.now())
                .build();

        when(usageEventService.getUsageEventsByCustomer(customerId))
                .thenReturn(List.of(event1, event2));
        when(usageEventMapper.toDtoList(anyList())).thenReturn(List.of(
                UsageEventDto.builder().customerId(customerId).serviceType("api-calls").build(),
                UsageEventDto.builder().customerId(customerId).serviceType("storage").build()
        ));

        mockMvc.perform(get("/api/v1/usage-events/customer/{customerId}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(customerContextService).verifyCustomerAccess(customerId);
        verify(usageEventService).getUsageEventsByCustomer(customerId);
    }

    @Test
    void shouldGetUsageEventsByCustomerAndServiceType() throws Exception {
        String customerId = "customer-1";
        String serviceType = "api-calls";
        UsageEvent event = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType(serviceType)
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        when(usageEventService.getUsageEventsByCustomerAndServiceType(customerId, serviceType))
                .thenReturn(List.of(event));
        when(usageEventMapper.toDtoList(anyList())).thenReturn(List.of(
                UsageEventDto.builder().customerId(customerId).serviceType(serviceType).build()
        ));

        mockMvc.perform(get("/api/v1/usage-events/customer/{customerId}/service/{serviceType}", customerId, serviceType))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].serviceType").value(serviceType));

        verify(customerContextService).verifyCustomerAccess(customerId);
        verify(usageEventService).getUsageEventsByCustomerAndServiceType(customerId, serviceType);
    }

    @Test
    void shouldRecordUsageEvent() throws Exception {
        // Given
        UUID eventId = UUID.randomUUID();
        UsageEventDto dto = UsageEventDto.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .build();

        UsageEvent savedEvent = UsageEvent.builder()
                .id(eventId)
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEventDto savedDto = UsageEventDto.builder()
                .id(eventId)
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .build();

        when(usageEventMapper.toEntity(any(UsageEventDto.class))).thenReturn(savedEvent);
        when(usageEventService.recordUsage(any(UsageEvent.class))).thenReturn(savedEvent);
        when(usageEventMapper.toDto(any(UsageEvent.class))).thenReturn(savedDto);

        // When & Then
        mockMvc.perform(post("/api/v1/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(eventId.toString()))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.serviceType").value("api-calls"))
                .andExpect(jsonPath("$.quantity").value(10.5));
    }

    @Test
    void shouldRejectInvalidUsageEvent() throws Exception {
        // Given
        UsageEventDto dto = UsageEventDto.builder()
                .customerId("")  // Invalid: empty
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}

