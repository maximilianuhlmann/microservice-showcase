# Integration Options: Apache Camel

## Why Apache Camel?

Apache Camel is an **integration framework** that provides a unified way to handle multiple protocols and APIs through a single routing engine. Instead of having separate controllers for HTTP, Kafka, GraphQL, etc., Camel allows you to define routes that can accept messages from any source and route them to your business logic.

## Benefits

1. **Unified Routing**: One route definition handles multiple protocols
2. **Protocol Abstraction**: Business logic doesn't care about transport layer
3. **Enterprise Integration Patterns**: Built-in support for EIPs (routing, transformation, etc.)
4. **Flexibility**: Easy to add new protocols without changing business logic
5. **Monitoring**: Built-in metrics and tracing

## Architecture with Camel

```
┌─────────────────────────────────────────────────────────┐
│              Apache Camel Routes                        │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐             │
│  │  HTTP    │  │  Kafka   │  │ GraphQL  │             │
│  │  REST    │  │ Consumer │  │ Resolver │             │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘             │
│       │              │              │                    │
│       └──────┬───────┴──────────────┘                    │
│              │                                            │
│              ▼                                            │
│      ┌───────────────┐                                   │
│      │  Camel Route   │                                   │
│      │  (unified)     │                                   │
│      └───────┬───────┘                                   │
└──────────────┼───────────────────────────────────────────┘
               │
               ▼
┌─────────────────────────────────────────────────────────┐
│         Business Logic (Service Layer)                  │
│  - UsageEventService                                    │
│  - BillingService                                       │
│  (Protocol-agnostic)                                     │
└─────────────────────────────────────────────────────────┘
```

## Implementation Example

### Step 1: Add Dependencies

```xml
<dependency>
    <groupId>org.apache.camel.springboot</groupId>
    <artifactId>camel-spring-boot-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.springboot</groupId>
    <artifactId>camel-http-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.springboot</groupId>
    <artifactId>camel-kafka-starter</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.springboot</groupId>
    <artifactId>camel-jackson-starter</artifactId>
</dependency>
```

### Step 2: Define Camel Routes

```java
@Component
@RequiredArgsConstructor
public class UsageEventRoute extends RouteBuilder {

    private final UsageEventService usageEventService;
    private final UsageEventMapper mapper;

    @Override
    public void configure() throws Exception {
        
        // HTTP REST endpoint
        from("rest:post:/api/v1/usage-events")
            .unmarshal().json(UsageEventDto.class)
            .process(exchange -> {
                UsageEventDto dto = exchange.getIn().getBody(UsageEventDto.class);
                UsageEvent event = mapper.mapToEntity(dto);
                UsageEvent saved = usageEventService.recordUsage(event);
                exchange.getIn().setBody(mapper.mapToDto(saved));
            })
            .marshal().json()
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

        // Kafka consumer
        from("kafka:usage-events?brokers=localhost:9092&groupId=billing-service")
            .unmarshal().json(UsageEventDto.class)
            .process(exchange -> {
                UsageEventDto dto = exchange.getIn().getBody(UsageEventDto.class);
                UsageEvent event = mapper.mapToEntity(dto);
                usageEventService.recordUsage(event);
            })
            .log("Processed usage event from Kafka: ${body}");

        // JMS queue (alternative)
        from("jms:queue:usage-events")
            .unmarshal().json(UsageEventDto.class)
            .process(exchange -> {
                UsageEventDto dto = exchange.getIn().getBody(UsageEventDto.class);
                UsageEvent event = mapper.mapToEntity(dto);
                usageEventService.recordUsage(event);
            });
    }
}
```

### Step 3: Unified Route (Advanced)

```java
@Component
@RequiredArgsConstructor
public class UnifiedUsageEventRoute extends RouteBuilder {

    private final UsageEventService usageEventService;
    private final UsageEventMapper mapper;

    @Override
    public void configure() throws Exception {
        
        // Unified route that accepts from multiple sources
        from("direct:processUsageEvent")
            .unmarshal().json(UsageEventDto.class)
            .process(exchange -> {
                UsageEventDto dto = exchange.getIn().getBody(UsageEventDto.class);
                UsageEvent event = mapper.mapToEntity(dto);
                UsageEvent saved = usageEventService.recordUsage(event);
                exchange.getIn().setBody(mapper.mapToDto(saved));
            })
            .marshal().json();

        // HTTP endpoint routes to unified route
        from("rest:post:/api/v1/usage-events")
            .to("direct:processUsageEvent")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));

        // Kafka routes to unified route
        from("kafka:usage-events?brokers=localhost:9092")
            .to("direct:processUsageEvent")
            .log("Processed from Kafka");

        // JMS routes to unified route
        from("jms:queue:usage-events")
            .to("direct:processUsageEvent")
            .log("Processed from JMS");
    }
}
```

## Supported Protocols

Camel supports **300+ components**, including:

- **HTTP/REST**: `rest`, `http`, `servlet`
- **Messaging**: `kafka`, `jms`, `rabbitmq`, `activemq`
- **GraphQL**: `graphql` (via extension)
- **File**: `file`, `ftp`, `sftp`
- **Database**: `jdbc`, `sql`
- **Cloud**: `aws-s3`, `aws-sqs`, `azure-servicebus`
- **And many more...**

## Advantages Over Separate Controllers

| Approach | Separate Controllers | Apache Camel |
|----------|---------------------|--------------|
| **Code Duplication** | Each protocol needs its own controller | Single route definition |
| **Protocol Changes** | Modify multiple controllers | Modify route configuration |
| **Testing** | Test each controller separately | Test route once |
| **Monitoring** | Separate metrics per protocol | Unified metrics |
| **Transformation** | Manual in each controller | Built-in transformers |
| **Error Handling** | Per-controller exception handlers | Unified error handling |

## Error Handling with Camel

```java
from("rest:post:/api/v1/usage-events")
    .doTry()
        .unmarshal().json(UsageEventDto.class)
        .to("direct:processUsageEvent")
    .doCatch(ValidationException.class)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(400))
        .setBody().constant("{\"error\":\"Validation failed\"}")
    .doCatch(Exception.class)
        .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
        .setBody().constant("{\"error\":\"Internal server error\"}")
    .end();
```

## Monitoring & Metrics

Camel provides built-in metrics:

```java
// Enable metrics
camel.springboot.metrics.enabled=true

// Access metrics
@Autowired
private CamelContext camelContext;

public void getMetrics() {
    RouteMetrics routeMetrics = camelContext.getRoute("usage-event-route")
        .getRouteContext()
        .getRouteMetrics();
    
    long messagesProcessed = routeMetrics.getExchangesCompleted();
    long failures = routeMetrics.getExchangesFailed();
}
```

## Comparison: Camel vs Separate Controllers

### Separate Controllers Approach (Current)
```
HTTP Controller → Service
Kafka Consumer → Service  
GraphQL Resolver → Service
JMS Listener → Service
```
**Issues:** Code duplication, separate error handling, separate monitoring

### Camel Approach (Proposed)
```
HTTP → Camel Route → Service
Kafka → Camel Route → Service
GraphQL → Camel Route → Service
JMS → Camel Route → Service
```
**Benefits:** Unified routing, single error handling, unified monitoring

## Migration Strategy

1. **Phase 1**: Keep existing REST controllers (current)
2. **Phase 2**: Add Camel alongside existing controllers
3. **Phase 3**: Migrate one protocol at a time to Camel
4. **Phase 4**: Remove old controllers once all protocols use Camel

## Recommendation

**Yes, Apache Camel is an excellent option** for this use case because:

1. **Unified Interface**: One route handles all protocols
2. **Protocol Agnostic**: Business logic doesn't change when adding new protocols
3. **Enterprise Ready**: Built-in patterns, monitoring, error handling
4. **Flexible**: Easy to add new protocols (just add a route)
5. **Maintainable**: Less code duplication, single place for transformations

**Best Approach:**
- Use Camel for **ingestion** (HTTP, Kafka, JMS, etc.)
- Keep REST controllers for **query/read operations** (simpler, more standard)
- Or use Camel for everything if you want complete unification

## Example: Complete Camel-Based Service

```java
@Component
public class BillingServiceRoutes extends RouteBuilder {
    
    @Override
    public void configure() {
        // Usage event ingestion (multiple protocols)
        from("rest:post:/api/v1/usage-events")
            .to("direct:recordUsageEvent");
            
        from("kafka:usage-events")
            .to("direct:recordUsageEvent");
            
        from("direct:recordUsageEvent")
            .unmarshal().json(UsageEventDto.class)
            .bean(usageEventService, "recordUsage")
            .marshal().json();
        
        // Billing calculation
        from("rest:post:/api/v1/billing/{customerId}/calculate")
            .setHeader("customerId", simple("${header.customerId}"))
            .setHeader("billingPeriod", simple("${header.billingPeriod}"))
            .bean(billingService, "calculateBilling")
            .marshal().json();
    }
}
```

This approach provides a **clean, unified integration layer** that can grow with your needs without cluttering your business logic with protocol-specific code.

