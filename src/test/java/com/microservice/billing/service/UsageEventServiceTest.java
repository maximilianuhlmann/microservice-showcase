package com.microservice.billing.service;

import com.microservice.billing.domain.UsageEvent;
import com.microservice.billing.repository.UsageEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsageEventServiceTest {

    @Mock
    private UsageEventRepository repository;

    @InjectMocks
    private UsageEventService service;

    @Test
    void shouldRecordUsageEvent() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        UsageEvent event = UsageEvent.builder()
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .timestamp(now)
                .build();

        UUID eventId = UUID.randomUUID();
        LocalDateTime createdAt = now.plusSeconds(1);
        UsageEvent savedEvent = UsageEvent.builder()
                .id(eventId)
                .customerId("customer-1")
                .serviceType("api-calls")
                .quantity(new BigDecimal("10.5"))
                .unit("requests")
                .timestamp(now)
                .createdAt(createdAt)
                .build();

        when(repository.save(any(UsageEvent.class))).thenReturn(savedEvent);

        // When
        UsageEvent result = service.recordUsage(event);

        // Then
        assertNotNull(result);
        assertEquals(eventId, result.getId());
        assertEquals("customer-1", result.getCustomerId());
        assertEquals("api-calls", result.getServiceType());
        assertNotNull(result.getCreatedAt());
        verify(repository, times(1)).save(any(UsageEvent.class));
    }

    @Test
    void shouldRetrieveUsageEventsByCustomerId() {
        // Given
        String customerId = "customer-1";
        UsageEvent event1 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("api-calls")
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        UsageEvent event2 = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType("storage")
                .quantity(new BigDecimal("20"))
                .unit("gb")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findByCustomerId(customerId)).thenReturn(List.of(event1, event2));

        // When
        List<UsageEvent> result = service.getUsageEventsByCustomer(customerId);

        // Then
        assertEquals(2, result.size());
        verify(repository, times(1)).findByCustomerId(customerId);
    }

    @Test
    void shouldRetrieveUsageEventsByCustomerAndServiceType() {
        // Given
        String customerId = "customer-1";
        String serviceType = "api-calls";
        
        UsageEvent event = UsageEvent.builder()
                .id(UUID.randomUUID())
                .customerId(customerId)
                .serviceType(serviceType)
                .quantity(new BigDecimal("10"))
                .unit("requests")
                .timestamp(LocalDateTime.now())
                .build();

        when(repository.findByCustomerIdAndServiceType(customerId, serviceType))
                .thenReturn(List.of(event));

        // When
        List<UsageEvent> result = service.getUsageEventsByCustomerAndServiceType(customerId, serviceType);

        // Then
        assertEquals(1, result.size());
        assertEquals(serviceType, result.get(0).getServiceType());
        verify(repository, times(1)).findByCustomerIdAndServiceType(customerId, serviceType);
    }
}

