package com.microservice.billing.repository;

import com.microservice.billing.domain.UsageEvent;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@org.springframework.test.context.ContextConfiguration(initializers = com.microservice.billing.config.PostgresTestContainer.class)
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=none",
        "spring.flyway.enabled=true"
})
class UsageEventRepositoryTest {

    @Autowired
    private UsageEventRepository repository;

    @Test
    void shouldSaveUsageEvent() {
        UsageEvent event = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent saved = repository.save(event);

        assertNotNull(saved.getId());
        assertEquals("customer-1", saved.getCustomerId());
        assertEquals("api-calls", saved.getServiceType());
        assertEquals(new BigDecimal("10.5"), saved.getQuantity());
    }

    @Test
    void shouldFindUsageEventsByCustomerId() {
        UsageEvent event1 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("storage")
                .quantity(new BigDecimal("20"))
                .unit("gb")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event3 = UsageEvent.builder()
                .customerId("customer-2")
                .serviceType("api-calls")
                .quantity(new BigDecimal("30"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        repository.saveAll(List.of(event1, event2, event3));

        List<UsageEvent> found = repository.findByCustomerId("customer-1");

        assertEquals(2, found.size());
        assertTrue(found.stream().allMatch(e -> e.getCustomerId().equals("customer-1")));
    }

    @Test
    void shouldFindUsageEventsByCustomerIdAndServiceType() {
        UsageEvent event1 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("storage")
                .quantity(new BigDecimal("20"))
                .unit("gb")
                .timestamp(LocalDateTime.now())
                .build();

        repository.saveAll(List.of(event1, event2));

        List<UsageEvent> found = repository.findByCustomerIdAndServiceType("customer-1", "api-calls");

        assertEquals(1, found.size());
        assertEquals("api-calls", found.get(0).getServiceType());
    }
}

