package com.microservice.billing.controller;

import com.microservice.billing.domain.BillingBreakdown;
import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.mapper.BillingRecordMapper;
import com.microservice.billing.service.BillingService;
import com.microservice.billing.service.CustomerContextService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BillingController.class, excludeAutoConfiguration = {
        org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class BillingControllerBreakdownTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private BillingRecordMapper billingRecordMapper;

    @MockBean
    private CustomerContextService customerContextService;


    @Test
    void shouldReturnBillingRecordWithBreakdown() throws Exception {
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);

        BillingRecord record = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("6.00"))
                .build();

        BillingBreakdown breakdown1 = BillingBreakdown.builder()
                .serviceType("api-calls")
                .quantity(new BigDecimal("1000"))
                .rate(new BigDecimal("0.001"))
                .amount(new BigDecimal("1.00"))
                .build();

        BillingBreakdown breakdown2 = BillingBreakdown.builder()
                .serviceType("storage")
                .quantity(new BigDecimal("50"))
                .rate(new BigDecimal("0.10"))
                .amount(new BigDecimal("5.00"))
                .build();

        BillingRecordDto dto = BillingRecordDto.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("6.00"))
                .breakdown(List.of(
                        BillingBreakdownDto.builder()
                                .serviceType("api-calls")
                                .quantity(new BigDecimal("1000"))
                                .rate(new BigDecimal("0.001"))
                                .amount(new BigDecimal("1.00"))
                                .build(),
                        BillingBreakdownDto.builder()
                                .serviceType("storage")
                                .quantity(new BigDecimal("50"))
                                .rate(new BigDecimal("0.10"))
                                .amount(new BigDecimal("5.00"))
                                .build()
                ))
                .build();

        when(billingService.calculateBilling(customerId, billingPeriod)).thenReturn(record);
        when(billingService.getBillingBreakdown(1L)).thenReturn(List.of(breakdown1, breakdown2));
        when(billingRecordMapper.toDto(any(BillingRecord.class), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/billing/{customerId}/calculate", customerId)
                        .param("billingPeriod", "2024-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.billingPeriod").value("2024-01"))
                .andExpect(jsonPath("$.totalAmount").value(6.00))
                .andExpect(jsonPath("$.breakdown").isArray())
                .andExpect(jsonPath("$.breakdown[0].serviceType").value("api-calls"))
                .andExpect(jsonPath("$.breakdown[0].quantity").value(1000))
                .andExpect(jsonPath("$.breakdown[0].rate").value(0.001))
                .andExpect(jsonPath("$.breakdown[0].amount").value(1.00))
                .andExpect(jsonPath("$.breakdown[1].serviceType").value("storage"))
                .andExpect(jsonPath("$.breakdown[1].quantity").value(50))
                .andExpect(jsonPath("$.breakdown[1].rate").value(0.10))
                .andExpect(jsonPath("$.breakdown[1].amount").value(5.00));
    }
}

