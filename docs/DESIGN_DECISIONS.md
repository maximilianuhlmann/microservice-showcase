# Design Decisions

## Folder Naming: `domain` vs `model`

### Decision: Use `domain`

**Rationale:**
- **Domain-Driven Design (DDD)**: The `domain` folder aligns with DDD principles, representing the core business domain entities
- **Clearer Intent**: "Domain" explicitly indicates these are business entities, not just data models
- **Industry Standard**: Most Spring Boot projects following DDD use `domain`
- **Separation**: Distinguishes from `dto` (data transfer objects) and `entity` (if using JPA-specific naming)

**Alternative Considered:**
- `model`: More generic, could be confused with DTOs or view models
- `entities`: Too JPA-specific, doesn't convey business meaning

**Current Structure:**
```
src/main/java/com/microservice/billing/
├── domain/          # Business domain entities (UsageEvent, BillingRecord, Customer)
├── dto/             # Data Transfer Objects (if we separate them)
├── controller/      # REST controllers
├── service/         # Business logic
├── repository/      # Data access
└── mapper/          # Entity ↔ DTO mapping
```

## GraphQL Support

### Should we add GraphQL?

**Pros:**
- **Flexible Queries**: Clients can request exactly the data they need
- **Single Endpoint**: `/graphql` instead of multiple REST endpoints
- **Type Safety**: Strong typing with schema
- **Efficient**: Reduces over-fetching and under-fetching

**Cons:**
- **Complexity**: Additional layer, schema management
- **Learning Curve**: Team needs GraphQL knowledge
- **Caching**: More complex than REST caching
- **Overkill for MVP**: Current REST API is sufficient

### Recommendation: **Yes, but as an extension**

**Implementation Strategy:**
1. Keep REST API as primary interface (current implementation)
2. Add GraphQL as additional interface using Spring GraphQL
3. Share the same service layer (no duplication)
4. Use feature flag to enable/disable GraphQL

**Structure:**
```
src/main/java/com/microservice/billing/
├── controller/
│   ├── rest/        # REST controllers (existing)
│   └── graphql/     # GraphQL resolvers (future)
```

## Message Queue Support (Apache Kafka)

### Should we add Kafka?

**Pros:**
- **High Throughput**: Handle millions of events per second
- **Decoupling**: Producers don't need to wait for processing
- **Scalability**: Horizontal scaling with partitions
- **Event Sourcing**: Natural fit for immutable event storage
- **Replay Capability**: Reprocess events for recalculation

**Cons:**
- **Infrastructure**: Requires Kafka cluster
- **Complexity**: Additional operational overhead
- **Eventual Consistency**: Async processing means delays
- **Overkill for MVP**: HTTP is sufficient for moderate load

### Recommendation: **Yes, as an alternative ingestion path**

**Implementation Strategy:**
1. Support both HTTP and Kafka for event ingestion
2. HTTP: Synchronous, immediate feedback (current)
3. Kafka: Asynchronous, high throughput (future)
4. Same service layer processes events from both sources

**Architecture:**
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
│  (same logic)    │
└─────────────────┘
```

**Structure:**
```
src/main/java/com/microservice/billing/
├── controller/
│   └── rest/           # HTTP REST (existing)
├── kafka/
│   ├── consumer/       # Kafka listeners
│   └── producer/       # If we need to publish events
└── service/
    └── UsageEventService  # Shared by both HTTP and Kafka
```

## Input Validation

### Current Validation

**UsageEventDto:**
- ✅ `@NotBlank` for customerId, serviceType, unit
- ✅ `@NotNull` and `@Positive` for quantity
- ✅ `@Size` constraints added for string fields

**Missing Validations:**
- ❌ Customer ID format validation (if there's a pattern)
- ❌ Service type enum validation (if limited set)
- ❌ Timestamp range validation (not in future, reasonable past)
- ❌ Metadata JSON format validation

### Recommendations

1. **Add Custom Validators:**
   ```java
   @ValidCustomerId
   private String customerId;
   
   @ValidServiceType
   private String serviceType;
   ```

2. **Add Timestamp Validation:**
   ```java
   @PastOrPresent(message = "Timestamp cannot be in the future")
   @NotNull
   private LocalDateTime timestamp;
   ```

3. **Add JSON Validation for Metadata:**
   ```java
   @JsonFormat
   @ValidJson
   private String metadata;
   ```

4. **Add Global Exception Handler:**
   ```java
   @ControllerAdvice
   public class ValidationExceptionHandler {
       @ExceptionHandler(MethodArgumentNotValidException.class)
       public ResponseEntity<ErrorResponse> handleValidationErrors(...)
   }
   ```

## Summary

| Decision | Choice | Rationale |
|----------|--------|-----------|
| Folder Name | `domain` | Aligns with DDD, clearer intent |
| GraphQL | Yes (extension) | Add as alternative interface, keep REST primary |
| Kafka | Yes (extension) | Add as alternative ingestion path, keep HTTP primary |
| Validation | Enhanced | Add custom validators and global exception handling |

