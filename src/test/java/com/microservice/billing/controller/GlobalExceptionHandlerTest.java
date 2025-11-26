package com.microservice.billing.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {UsageEventController.class}, excludeAutoConfiguration = {
        org.togglz.spring.boot.actuate.autoconfigure.TogglzAutoConfiguration.class,
        org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class
})
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.microservice.billing.service.UsageEventService usageEventService;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.microservice.billing.mapper.UsageEventMapper usageEventMapper;

    @org.springframework.boot.test.mock.mockito.MockBean
    private com.microservice.billing.service.CustomerContextService customerContextService;

    @Test
    void shouldHandleMalformedJson() throws Exception {
        mockMvc.perform(post("/api/v1/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ invalid json }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Malformed request body. Please check your JSON format."));
    }

    @Test
    void shouldHandleValidationErrors() throws Exception {
        UsageEventDto invalidDto = UsageEventDto.builder()
                .customerId("")  // Invalid: empty
                .serviceType("")  // Invalid: empty
                .quantity(null)  // Invalid: null
                .build();

        mockMvc.perform(post("/api/v1/usage-events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Validation Failed"))
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors").isArray())
                .andExpect(jsonPath("$.validationErrors.length()").value(greaterThanOrEqualTo(3)));
    }

    @Test
    void shouldHandleConstraintViolationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = mock(ServletWebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("customerId");
        when(violation.getMessage()).thenReturn("must not be blank");
        when(violation.getInvalidValue()).thenReturn("");
        violations.add(violation);

        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        var response = handler.handleConstraintViolation(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Constraint Violation", response.getBody().getError());
        assertNotNull(response.getBody().getValidationErrors());
        assertEquals(1, response.getBody().getValidationErrors().size());
    }

    @Test
    void shouldHandleCustomerAccessDeniedException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = mock(ServletWebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        com.microservice.billing.exception.CustomerAccessDeniedException ex = 
                new com.microservice.billing.exception.CustomerAccessDeniedException("customer-123", "customer-456");

        var response = handler.handleCustomerAccessDenied(ex, request);

        assertEquals(403, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Forbidden", response.getBody().getError());
        assertEquals("Access denied: You can only access your own data", response.getBody().getMessage());
    }

    @Test
    void shouldHandleFeatureDisabledException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = mock(ServletWebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        com.microservice.billing.exception.FeatureDisabledException ex = 
                new com.microservice.billing.exception.FeatureDisabledException("USAGE_AGGREGATION");

        var response = handler.handleFeatureDisabled(ex, request);

        assertEquals(409, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Feature Disabled", response.getBody().getError());
        assertEquals("Feature 'USAGE_AGGREGATION' is disabled", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDomainValidationException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = mock(ServletWebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        com.microservice.billing.exception.DomainValidationException ex = 
                new com.microservice.billing.exception.DomainValidationException("customerId", null, "Customer ID cannot be null or blank");

        var response = handler.handleDomainValidation(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Validation Failed", response.getBody().getError());
        assertEquals("Customer ID cannot be null or blank", response.getBody().getMessage());
    }

    @Test
    void shouldHandleDateTimeParseException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = mock(ServletWebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        java.time.format.DateTimeParseException ex = new java.time.format.DateTimeParseException(
                "Text '2024-01-01' could not be parsed", "2024-01-01", 7);

        var response = handler.handleDateTimeParseException(ex, request);

        assertEquals(400, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Bad Request", response.getBody().getError());
        assertTrue(response.getBody().getMessage().contains("Invalid date format"));
        assertTrue(response.getBody().getMessage().contains("2024-01-01"));
        assertTrue(response.getBody().getMessage().contains("YYYY-MM"));
    }

    @Test
    void shouldHandleGenericException() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();
        ServletWebRequest request = mock(ServletWebRequest.class);
        when(request.getDescription(false)).thenReturn("uri=/api/v1/test");

        RuntimeException ex = new RuntimeException("Unexpected error");

        var response = handler.handleGenericException(ex, request);

        assertEquals(500, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("Internal Server Error", response.getBody().getError());
        assertEquals("An unexpected error occurred", response.getBody().getMessage());
    }
}

