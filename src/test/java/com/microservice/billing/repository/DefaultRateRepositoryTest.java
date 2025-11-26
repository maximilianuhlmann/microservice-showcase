package com.microservice.billing.repository;

import com.microservice.billing.domain.DefaultRate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ContextConfiguration(initializers = com.microservice.billing.config.PostgresTestContainer.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
class DefaultRateRepositoryTest {

    @Autowired
    private DefaultRateRepository repository;

    @Test
    void shouldFindDefaultRateByServiceType() {
        Optional<DefaultRate> found = repository.findByServiceTypeAndActiveTrue("api-calls");

        assertTrue(found.isPresent());
        assertEquals(0, new BigDecimal("0.001").compareTo(found.get().getRate()));
    }

    @Test
    void shouldNotFindInactiveDefaultRate() {
        DefaultRate rate = DefaultRate.builder()
                .serviceType("custom-service")
                .rate(new BigDecimal("0.05"))
                .active(false)
                .build();

        repository.save(rate);

        Optional<DefaultRate> found = repository.findByServiceTypeAndActiveTrue("custom-service");

        assertFalse(found.isPresent());
    }

    @Test
    void shouldFindDefaultRatesFromMigration() {
        assertTrue(repository.findByServiceTypeAndActiveTrue("api-calls").isPresent());
        assertTrue(repository.findByServiceTypeAndActiveTrue("storage").isPresent());
        assertTrue(repository.findByServiceTypeAndActiveTrue("compute").isPresent());
        assertTrue(repository.findByServiceTypeAndActiveTrue("data-transfer").isPresent());
    }
}

