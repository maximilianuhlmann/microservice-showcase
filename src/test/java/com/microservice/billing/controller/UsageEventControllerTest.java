package com.microservice.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.service.UsageEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UsageEventController.class)
class UsageEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsageEventService usageEventService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldRecordUsageEvent() throws Exception {
        // Given
        UsageEventDto dto = UsageEventDto.builder()
                .customerId("customer-1")
                .serviceId("service-1")
                .quantity(new BigDecimal("10.5"))
                .build();

        UsageEvent savedEvent = UsageEvent.builder()
                .id(1L)
                .customerId("customer-1")
                .serviceId("service-1")
                .quantity(new BigDecimal("10.5"))
                .timestamp(LocalDateTime.now())
                .build();

        when(usageEventService.recordUsage(any(UsageEvent.class))).thenReturn(savedEvent);

        // When & Then
        mockMvc.perform(post("/api/v1/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value("customer-1"))
                .andExpect(jsonPath("$.serviceId").value("service-1"))
                .andExpect(jsonPath("$.quantity").value(10.5));
    }

    @Test
    void shouldRejectInvalidUsageEvent() throws Exception {
        // Given
        UsageEventDto dto = UsageEventDto.builder()
                .customerId("")  // Invalid: empty
                .serviceId("service-1")
                .quantity(new BigDecimal("10.5"))
                .build();

        // When & Then
        mockMvc.perform(post("/api/v1/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }
}

