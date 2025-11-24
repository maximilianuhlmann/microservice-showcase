# Future Extensions: GraphQL, Kafka & Apache Camel

> **Note**: For a unified integration approach supporting multiple protocols, see [INTEGRATION.md](INTEGRATION.md) which covers Apache Camel as a comprehensive solution.

## GraphQL Extension

### Why GraphQL?

- **Flexible Queries**: Clients request exactly what they need
- **Single Endpoint**: `/graphql` instead of multiple REST endpoints
- **Type Safety**: Strong typing with schema
- **Efficient**: Reduces over-fetching and under-fetching

### Implementation Plan

**Step 1: Add Dependencies**
```xml
<dependency>
    <groupId>org.springframework.graphql</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
```

**Step 2: Create GraphQL Schema**
```graphql
# schema.graphqls
type Query {
    usageEvents(customerId: String!): [UsageEvent!]!
    billingRecord(customerId: String!, billingPeriod: Date!): BillingRecord
}

type Mutation {
    recordUsageEvent(input: UsageEventInput!): UsageEvent!
    calculateBilling(customerId: String!, billingPeriod: Date!): BillingRecord!
}

type UsageEvent {
    id: ID!
    customerId: String!
    serviceType: String!
    quantity: Decimal!
    unit: String!
    timestamp: DateTime!
}
```

**Step 3: Create Resolvers**
```java
@Controller
public class UsageEventGraphQLController {
    
    @QueryMapping
    public List<UsageEventDto> usageEvents(@Argument String customerId) {
        // Reuse existing service
    }
    
    @MutationMapping
    public UsageEventDto recordUsageEvent(@Argument UsageEventInput input) {
        // Reuse existing service
    }
}
```

**Structure:**
```
src/main/java/com/microservice/billing/
├── controller/
│   ├── rest/           # Existing REST controllers
│   └── graphql/        # GraphQL resolvers (future)
└── resources/
    └── graphql/
        └── schema.graphqls
```

## Kafka Extension

### Why Kafka?

- **High Throughput**: Handle millions of events per second
- **Decoupling**: Producers don't wait for processing
- **Scalability**: Horizontal scaling with partitions
- **Event Sourcing**: Natural fit for immutable events
- **Replay Capability**: Reprocess events for recalculation

### Implementation Plan

**Step 1: Add Dependencies**
```xml
<dependency>
    <groupId>org.springframework.kafka</groupId>
    <artifactId>spring-kafka</artifactId>
</dependency>
```

**Step 2: Create Kafka Consumer**
```java
@Component
@RequiredArgsConstructor
public class UsageEventKafkaConsumer {
    
    private final UsageEventService usageEventService;
    private final UsageEventMapper mapper;
    
    @KafkaListener(topics = "usage-events", groupId = "billing-service")
    public void consumeUsageEvent(UsageEventMessage message) {
        UsageEventDto dto = mapFromMessage(message);
        usageEventService.recordUsage(mapper.mapToEntity(dto));
    }
}
```

**Step 3: Configuration**
```yaml
spring:
  kafka:
    bootstrap-servers: localhost:9092
    consumer:
      group-id: billing-service
      auto-offset-reset: earliest
```

**Structure:**
```
src/main/java/com/microservice/billing/
├── controller/
│   └── rest/           # HTTP REST (existing)
├── kafka/
│   ├── consumer/       # Kafka listeners
│   ├── producer/       # If we publish events
│   └── dto/            # Kafka message DTOs
└── service/
    └── UsageEventService  # Shared by both HTTP and Kafka
```

## Hybrid Architecture

Both HTTP and Kafka can coexist:

```
┌─────────────────┐
│ External Service│
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
    ▼         ▼
┌────────┐ ┌────────┐
│  HTTP  │ │ Kafka  │
│  REST  │ │Topic   │
└────┬───┘ └───┬────┘
     │         │
     └────┬────┘
          │
          ▼
┌─────────────────┐
│ UsageEventService│
│  (shared logic)  │
└─────────────────┘
```

**Benefits:**
- HTTP for synchronous, immediate feedback
- Kafka for asynchronous, high-throughput scenarios
- Same business logic, different transport layers

## Feature Flags

Use existing feature flags to control extensions:

```java
if (Features.KAFKA_INGESTION.isActive()) {
    // Process from Kafka
} else {
    // Process from HTTP
}
```

## Migration Strategy

1. **Phase 1**: Keep HTTP REST as primary (current)
2. **Phase 2**: Add GraphQL alongside REST (optional interface)
3. **Phase 3**: Add Kafka for high-volume scenarios (alternative ingestion)
4. **Phase 4**: Monitor and optimize based on usage patterns

