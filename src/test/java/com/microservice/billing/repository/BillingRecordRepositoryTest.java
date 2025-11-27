package com.microservice.billing.repository;

import com.microservice.billing.domain.BillingRecord;
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
class BillingRecordRepositoryTest {

    @Autowired
    private BillingRecordRepository repository;

    @Test
    void shouldSaveBillingRecord() {
        BillingRecord billingRecord = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("100.50"))
                .build();

        BillingRecord saved = repository.save(billingRecord);

        assertNotNull(saved.getId());
        assertEquals("customer-1", saved.getCustomerId());
        assertEquals("2024-01", saved.getBillingPeriod());
        assertEquals(new BigDecimal("100.50"), saved.getTotalAmount());
    }

    @Test
    void shouldFindBillingRecordByCustomerIdAndPeriod() {
        BillingRecord billingRecord = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod("2024-01")
                .totalAmount(new BigDecimal("100.50"))
                .build();

        repository.save(billingRecord);

        Optional<BillingRecord> found = repository.findByCustomerIdAndBillingPeriod(
                "customer-1",
                "2024-01"
        );

        assertTrue(found.isPresent());
        assertEquals("customer-1", found.get().getCustomerId());
        assertEquals("2024-01", found.get().getBillingPeriod());
        assertEquals(new BigDecimal("100.50"), found.get().getTotalAmount());
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        Optional<BillingRecord> found = repository.findByCustomerIdAndBillingPeriod(
                "customer-999",
                "2024-01"
        );

        assertFalse(found.isPresent());
    }
}

