package com.microservice.billing.integration.stepdefs;

import com.microservice.billing.controller.BillingRecordDto;
import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.UsageEventRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Component
public class BillingStepDefinitions {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UsageEventRepository usageEventRepository;

    @LocalServerPort
    private int port;

    private ResponseEntity<BillingRecordDto> response;
    private String baseUrl;
    private String customerId;
    private String billingPeriod;

    @Given("the billing API is available")
    public void theBillingApiIsAvailable() {
        baseUrl = "http://localhost:" + port + "/api";
    }

    @Given("customer {string} has usage events in period {string}")
    public void customerHasUsageEventsInPeriod(String customer, String period) {
        this.customerId = customer;
        this.billingPeriod = period;
        
        YearMonth periodYearMonth = YearMonth.parse(period);
        UsageEvent event1 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customer)
                .serviceType("api-calls")
                .quantity(new BigDecimal("100.0"))
                .unit("requests")
                .timestamp(periodYearMonth.atDay(5).atStartOfDay())
                .build();
        
        UsageEvent event2 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customer)
                .serviceType("api-calls")
                .quantity(new BigDecimal("200.0"))
                .unit("requests")
                .timestamp(periodYearMonth.atDay(15).atStartOfDay())
                .build();
        
        usageEventRepository.save(event1);
        usageEventRepository.save(event2);
    }

    @When("I calculate billing for customer {string} and period {string}")
    public void iCalculateBillingForCustomerAndPeriod(String customer, String period) {
        response = restTemplate.exchange(
                baseUrl + "/billing/" + customer + "/calculate?billingPeriod=" + period,
                HttpMethod.POST,
                null,
                BillingRecordDto.class
        );
    }

    @Then("the billing record should be created")
    public void theBillingRecordShouldBeCreated() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        BillingRecordDto billingRecord = response.getBody();
        assertEquals(customerId, billingRecord.getCustomerId());
        assertEquals(billingPeriod, billingRecord.getBillingPeriod());
        assertNotNull(billingRecord.getTotalAmount());
        assertTrue(billingRecord.getTotalAmount().compareTo(BigDecimal.ZERO) > 0);
    }

    @Then("the total amount should be {string}")
    public void theTotalAmountShouldBe(String expectedAmount) {
        assertNotNull(response.getBody());
        BigDecimal expected = new BigDecimal(expectedAmount);
        assertEquals(0, expected.compareTo(response.getBody().getTotalAmount()),
                "Expected total amount: " + expectedAmount);
    }

    @Then("the total amount should be greater than {string}")
    public void theTotalAmountShouldBeGreaterThan(String expectedAmount) {
        assertNotNull(response.getBody());
        BigDecimal expected = new BigDecimal(expectedAmount);
        BigDecimal actual = response.getBody().getTotalAmount();
        assertTrue(actual.compareTo(expected) > 0,
                "Expected total amount to be greater than " + expectedAmount + ", but was " + actual);
    }

    @When("I retrieve the billing record for customer {string} and period {string}")
    public void iRetrieveTheBillingRecordForCustomerAndPeriod(String customer, String period) {
        response = restTemplate.exchange(
                baseUrl + "/billing/" + customer + "?billingPeriod=" + period,
                HttpMethod.GET,
                null,
                BillingRecordDto.class
        );
    }

    @Then("the billing record should be found")
    public void theBillingRecordShouldBeFound() {
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @When("I get usage aggregation by service type for customer {string}")
    public void iGetUsageAggregationByServiceTypeForCustomer(String customer) {
    }

    @Then("I should receive aggregated usage data")
    public void iShouldReceiveAggregatedUsageData() {
    }
}

