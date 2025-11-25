package com.microservice.billing.integration.stepdefs;

import com.microservice.billing.controller.UsageEventDto;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.UsageEventRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Component
public class UsageEventStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsageEventRepository usageEventRepository;

    @LocalServerPort
    private int port;

    private ResponseEntity<UsageEventDto> response;
    private ResponseEntity<List<UsageEventDto>> listResponse;
    private UsageEventDto usageEventDto;
    private String baseUrl;

    @Given("the API is available")
    public void theApiIsAvailable() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Given("a usage event with customer ID {string}, service type {string}, and quantity {string}")
    public void aUsageEventWithCustomerIdServiceTypeAndQuantity(String customerId, String serviceType, String quantity) {
        usageEventDto = UsageEventDto.builder()
                .customerId(customerId)
                .serviceType(serviceType)
                .quantity(new BigDecimal(quantity))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();
    }

    @When("I create the usage event")
    public void iCreateTheUsageEvent() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");
        HttpEntity<UsageEventDto> request = new HttpEntity<>(usageEventDto, headers);
        
        response = restTemplate.exchange(
                baseUrl + "/usage-events",
                HttpMethod.POST,
                request,
                UsageEventDto.class
        );
    }

    @Then("the usage event should be created successfully")
    public void theUsageEventShouldBeCreatedSuccessfully() {
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getId());
        assertEquals(usageEventDto.getCustomerId(), response.getBody().getCustomerId());
        assertEquals(usageEventDto.getServiceType(), response.getBody().getServiceType());
    }

    @Given("usage events exist for customer {string}")
    public void usageEventsExistForCustomer(String customerId) {
        UsageEvent event1 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();
        
        UsageEvent event2 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("storage")
                .quantity(new BigDecimal("5.0"))
                .unit("GB")
                .timestamp(LocalDateTime.now())
                .build();
        
        usageEventRepository.save(event1);
        usageEventRepository.save(event2);
    }

    @When("I retrieve usage events for customer {string}")
    public void iRetrieveUsageEventsForCustomer(String customerId) {
        listResponse = restTemplate.exchange(
                baseUrl + "/usage-events/customer/" + customerId,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UsageEventDto>>() {}
        );
    }

    @Then("I should receive {int} usage events")
    public void iShouldReceiveUsageEvents(int expectedCount) {
        assertEquals(HttpStatus.OK, listResponse.getStatusCode());
        assertNotNull(listResponse.getBody());
        assertEquals(expectedCount, listResponse.getBody().size());
    }

    @When("I retrieve usage events for customer {string} and service type {string}")
    public void iRetrieveUsageEventsForCustomerAndServiceType(String customerId, String serviceType) {
        listResponse = restTemplate.exchange(
                baseUrl + "/usage-events/customer/" + customerId + "/service/" + serviceType,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<UsageEventDto>>() {}
        );
    }

    @Then("the response status should be {int}")
    public void theResponseStatusShouldBe(int statusCode) {
        HttpStatus expectedStatus = HttpStatus.valueOf(statusCode);
        if (response != null) {
            assertEquals(expectedStatus, response.getStatusCode());
        } else if (listResponse != null) {
            assertEquals(expectedStatus, listResponse.getStatusCode());
        }
    }
}

