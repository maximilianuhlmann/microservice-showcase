package com.microservice.billing.repository;

import com.microservice.billing.domain.UsageEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsageEventRepositoryTest {

    @Autowired
    private UsageEventRepository repository;

    @Test
    void shouldSaveUsageEvent() {
        // Given
        UsageEvent event = UsageEvent.builder()
                .customerId("customer-1")
                .serviceId("service-1")
                .quantity(new BigDecimal("10.5"))
                .timestamp(LocalDateTime.now())
                .build();

        // When
        UsageEvent saved = repository.save(event);

        // Then
        assertNotNull(saved.getId());
        assertEquals("customer-1", saved.getCustomerId());
        assertEquals("service-1", saved.getServiceId());
        assertEquals(new BigDecimal("10.5"), saved.getQuantity());
    }

    @Test
    void shouldFindUsageEventsByCustomerId() {
        // Given
        UsageEvent event1 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceId("service-1")
                .quantity(new BigDecimal("10"))
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceId("service-2")
                .quantity(new BigDecimal("20"))
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event3 = UsageEvent.builder()
                .customerId("customer-2")
                .serviceId("service-1")
                .quantity(new BigDecimal("30"))
                .timestamp(LocalDateTime.now())
                .build();

        repository.saveAll(List.of(event1, event2, event3));

        // When
        List<UsageEvent> found = repository.findByCustomerId("customer-1");

        // Then
        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(e -> e.getCustomerId().equals("customer-1")));
    }

    @Test
    void shouldFindUsageEventsByCustomerIdAndServiceId() {
        // Given
        UsageEvent event1 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceId("service-1")
                .quantity(new BigDecimal("10"))
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceId("service-2")
                .quantity(new BigDecimal("20"))
                .timestamp(LocalDateTime.now())
                .build();

        repository.saveAll(List.of(event1, event2));

        // When
        List<UsageEvent> found = repository.findByCustomerIdAndServiceId("customer-1", "service-1");

        // Then
        assertEquals(1, found.size());
        assertEquals("service-1", found.get(0).getServiceId());
    }
}

