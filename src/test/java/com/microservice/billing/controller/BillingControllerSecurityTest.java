package com.microservice.billing.controller;

import com.microservice.billing.domain.BillingRecord;
import com.microservice.billing.exception.CustomerAccessDeniedException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = BillingController.class, excludeAutoConfiguration = {
        org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class BillingControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BillingService billingService;

    @MockBean
    private BillingRecordMapper billingRecordMapper;

    @MockBean
    private CustomerContextService customerContextService;

    @Test
    void shouldDenyAccessWhenCustomerIdsDontMatch() throws Exception {
        String otherCustomerId = "customer-2";

        doThrow(new CustomerAccessDeniedException("customer-1", "customer-2"))
                .when(customerContextService).verifyCustomerAccess(otherCustomerId);

        mockMvc.perform(post("/api/v1/billing/{customerId}/calculate", otherCustomerId)
                        .param("billingPeriod", "2024-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Access denied: You can only access your own data"));
    }

    @Test
    void shouldAllowAccessWhenCustomerIdsMatch() throws Exception {
        String customerId = "customer-1";
        YearMonth billingPeriod = YearMonth.of(2024, 1);

        BillingRecord billingRecord = BillingRecord.builder()
                .id(1L)
                .customerId(customerId)
                .billingPeriod("2024-01")
                .totalAmount(BigDecimal.ZERO)
                .build();

        when(billingService.calculateBilling(customerId, billingPeriod)).thenReturn(billingRecord);
        when(billingService.getBillingBreakdown(1L)).thenReturn(java.util.Collections.emptyList());
        when(billingRecordMapper.toDto(any(BillingRecord.class), any())).thenReturn(BillingRecordDto.builder().build());

        mockMvc.perform(post("/api/v1/billing/{customerId}/calculate", customerId)
                        .param("billingPeriod", "2024-01")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}

