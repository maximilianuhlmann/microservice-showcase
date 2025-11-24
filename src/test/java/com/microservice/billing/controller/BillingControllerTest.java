package com.microservice.billing.controller;

import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.mapper.BillingRecordMapper;
import com.microservice.billing.service.BillingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BillingController.class, excludeAutoConfiguration = {
        org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration.class
})
class BillingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private BillingRecordMapper billingRecordMapper;

    @Test
    void shouldCalculateBilling() throws Exception {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);

        BillingRecord record = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(new BigDecimal("100.50"))
                .build();

        BillingRecordDto dto = BillingRecordDto.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(new BigDecimal("100.50"))
                .build();

        when(billingService.calculateBilling(customerId, billingPeriod)).thenReturn(record);
        when(billingRecordMapper.toDto(any(BillingRecord.class))).thenReturn(dto);

        // When & Then
        mockMvc.perform(post("/api/v1/billing/{customerId}/calculate", customerId)
                        .param("billingPeriod", billingPeriod.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.totalAmount").value(100.50));
    }

    @Test
    void shouldGetBillingRecord() throws Exception {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);

        BillingRecord record = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(new BigDecimal("100.50"))
                .build();

        BillingRecordDto dto = BillingRecordDto.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod(billingPeriod)
                .totalAmount(new BigDecimal("100.50"))
                .build();

        when(billingService.getBillingRecord(customerId, billingPeriod))
                .thenReturn(Optional.of(record));
        when(billingRecordMapper.toDto(any(BillingRecord.class))).thenReturn(dto);

        // When & Then
        mockMvc.perform(get("/api/v1/billing/{customerId}", customerId)
                        .param("billingPeriod", billingPeriod.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.totalAmount").value(100.50));
    }

    @Test
    void shouldReturnNotFoundWhenBillingRecordNotExists() throws Exception {
        // Given
        String customerId = "customer-1";
        LocalDate billingPeriod = LocalDate.of(2024, 1, 1);

        when(billingService.getBillingRecord(customerId, billingPeriod))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/v1/billing/{customerId}", customerId)
                        .param("billingPeriod", billingPeriod.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}

