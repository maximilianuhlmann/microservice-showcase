package com.microservice.billing.repository;

import com.microservice.billing.domain.Customer;
import com.microservice.billing.domain.PricingRate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ContextConfiguration(initializers = com.microservice.billing.config.PostgresTestContainer.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
class PricingRateRepositoryTest {

    @Autowired
    private PricingRateRepository repository;

    @Autowired
    private com.microservice.billing.repository.CustomerRepository customerRepository;

    @BeforeEach
    void setUp() {
        Customer customer = Customer.builder()
                .customerId("customer-1")
                .name("Test Customer")
                .active(true)
                .build();
        customerRepository.save(customer);
    }

    @Test
    void shouldSaveAndFindPricingRate() {
        PricingRate rate = PricingRate.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .rate(new BigDecimal("0.002"))
                .active(true)
                .build();

        PricingRate saved = repository.save(rate);

        assertNotNull(saved.getId());
        Optional<PricingRate> found = repository.findByCustomerIdAndServiceTypeAndActiveTrue(
                "customer-1", "api-calls");

        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("0.002").compareTo(found.get().getRate()));
    }

    @Test
    void shouldNotFindInactiveRates() {
        PricingRate rate = PricingRate.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .rate(new BigDecimal("0.002"))
                .active(false)
                .build();

        repository.save(rate);

        Optional<PricingRate> found = repository.findByCustomerIdAndServiceTypeAndActiveTrue(
                "customer-1", "api-calls");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindAllRatesForCustomer() {
        PricingRate rate1 = PricingRate.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .rate(new BigDecimal("0.002"))
                .active(true)
                .build();

        PricingRate rate2 = PricingRate.builder()
                .customerId("customer-1")
                .serviceType("storage")
                .rate(new BigDecimal("0.15"))
                .active(true)
                .build();

        repository.save(rate1);
        repository.save(rate2);

        List<PricingRate> found = repository.findByCustomerIdAndActiveTrue("customer-1");

        assertEquals(2, found.size());
    }
}

