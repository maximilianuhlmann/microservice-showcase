package com.microservice.billing.repository;

import com.microservice.billing.domain.BillingRecord;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
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
        // Given
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod(LocalDate.of(2024, 1, 1))
                .totalAmount(new BigDecimal("100.50"))
                .build();

        // When
        BillingRecord saved = repository.save(record);

        // Then
        assertNotNull(saved.getId());
        assertEquals("customer-1", saved.getCustomerId());
        assertEquals(new BigDecimal("100.50"), saved.getTotalAmount());
    }

    @Test
    void shouldFindBillingRecordByCustomerIdAndPeriod() {
        // Given
        BillingRecord record = BillingRecord.builder()
                .customerId("customer-1")
                .billingPeriod(LocalDate.of(2024, 1, 1))
                .totalAmount(new BigDecimal("100.50"))
                .build();

        repository.save(record);

        // When
        Optional<BillingRecord> found = repository.findByCustomerIdAndBillingPeriod(
                "customer-1",
                LocalDate.of(2024, 1, 1)
        );

        // Then
        assertTrue(found.isPresent());
        assertEquals("customer-1", found.get().getCustomerId());
        assertEquals(new BigDecimal("100.50"), found.get().getTotalAmount());
    }

    @Test
    void shouldReturnEmptyWhenRecordNotFound() {
        // When
        Optional<BillingRecord> found = repository.findByCustomerIdAndBillingPeriod(
                "customer-999",
                LocalDate.of(2024, 1, 1)
        );

        // Then
        assertFalse(found.isPresent());
    }
}

