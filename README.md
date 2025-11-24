# Usage Billing Service - MVP

A minimal viable product (MVP) for usage-based billing built with Java Spring Boot, following **TDD principles** and **MVC architecture**.

## Technology Stack

- **Java 21** (LTS)
- **Spring Boot 3.2.0**
- **Spring Data JPA** (Hibernate)
- **H2 Database** (in-memory for development)
- **Maven** (build tool)
- **Lombok** (reducing boilerplate)
- **JUnit 5** (for testing)
- **Testcontainers** (for integration tests)
- **Togglz** (feature flags)
- **SpringDoc OpenAPI** (API documentation)

## Architecture

Built using **MVC (Model-View-Controller)** pattern:

- **Model**: Domain entities (`UsageEvent`, `BillingRecord`, `Customer`, `BillingCycle`)
- **View**: REST API with DTOs
- **Controller**: REST controllers (`UsageEventController`, `BillingController`)
- **Service**: Business logic layer
- **Repository**: Data access layer

## Features

### MVP Features

1. ✅ **Record Usage Events**: Track usage events (customer, service type, quantity, unit)
2. ✅ **Retrieve Usage Events**: Get usage events by customer and/or service type
3. ✅ **Calculate Billing**: Aggregate usage and calculate billing amounts (with feature flags)
4. ✅ **Feature Flags**: Togglz-based feature flags for toggling features on/off
5. ✅ **OpenAPI Documentation**: Swagger UI for API documentation

### Feature Flags (Togglz)

Configured in `application.properties`:
- `togglz.usage-aggregation=true` - Enable/disable usage aggregation
- `togglz.realtime-billing=false` - Enable real-time billing (future)
- `togglz.invoice-generation=false` - Enable invoice generation (future)
- `togglz.webhook-notifications=false` - Enable webhooks (future)
- `togglz.advanced-metrics=false` - Enable advanced metrics (future)

Usage in code:
```java
if (Features.USAGE_AGGREGATION.isActive()) {
    // Feature is enabled
}
```

## Project Structure

```
src/
├── main/
│   ├── java/com/microservice/billing/
│   │   ├── UsageBillingApp.java
│   │   ├── config/
│   │   │   ├── Features.java          # Feature flags enum
│   │   │   ├── TogglzConfig.java     # Togglz configuration
│   │   │   ├── FeatureFlags.java     # Legacy wrapper (deprecated)
│   │   │   └── OpenApiConfig.java    # OpenAPI/Swagger config
│   │   ├── domain/
│   │   │   ├── UsageEvent.java
│   │   │   ├── BillingRecord.java
│   │   │   ├── Customer.java
│   │   │   └── BillingCycle.java
│   │   ├── repository/
│   │   │   ├── UsageEventRepository.java
│   │   │   └── BillingRecordRepository.java
│   │   ├── service/
│   │   │   ├── UsageEventService.java
│   │   │   └── BillingService.java
│   │   └── controller/
│   │       ├── UsageEventController.java
│   │       ├── UsageEventDto.java
│   │       ├── BillingController.java
│   │       └── BillingRecordDto.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/microservice/billing/
        ├── domain/
        ├── repository/
        ├── service/
        └── controller/
```

## Getting Started

### Prerequisites

- **Java 21** or higher
- **Maven 3.6+**

### Running the Application

```bash
# Run with Maven
mvn spring-boot:run

# Or build and run
mvn clean package
java -jar target/usage-billing-service-1.0.0-SNAPSHOT.jar
```

The application will start on `http://localhost:8080`

### Running Tests

```bash
# Run all tests
mvn test

# Run only unit tests
mvn test -Dtest=*Test

# Run only integration tests
mvn test -Dtest=*IT
```

## API Endpoints

### Usage Events

- **POST** `/api/v1/usage-events` - Record a usage event
- **GET** `/api/v1/usage-events/customer/{customerId}` - Get usage events for a customer
- **GET** `/api/v1/usage-events/customer/{customerId}/service/{serviceType}` - Get usage events filtered by service type

### Billing

- **POST** `/api/v1/billing/{customerId}/calculate?billingPeriod=2024-01-01` - Calculate billing
- **GET** `/api/v1/billing/{customerId}?billingPeriod=2024-01-01` - Get billing record

### API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

### Example Requests

**Record Usage Event:**
```bash
curl -X POST http://localhost:8080/api/v1/usage-events \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "customer-123",
    "serviceType": "api-calls",
    "quantity": 10.5,
    "unit": "requests"
  }'
```

**Get Usage Events:**
```bash
curl http://localhost:8080/api/v1/usage-events/customer/customer-123
```

**Calculate Billing:**
```bash
curl -X POST "http://localhost:8080/api/v1/billing/customer-123/calculate?billingPeriod=2024-01-01"
```

## Development Approach

This MVP was built using **Test-Driven Development (TDD)**:

1. ✅ Write tests first
2. ✅ Implement minimal code to make tests pass
3. ✅ Refactor if needed
4. ✅ Repeat

All layers have comprehensive test coverage:
- Domain model tests
- Repository tests (using `@DataJpaTest`)
- Service tests (using mocks)
- Controller tests (using `@WebMvcTest`)

## Testing

### Test Data

Test data is available in `src/test/resources/test-data.sql` for integration tests:
- Test customers (customer-123, customer-456)
- Sample usage events
- Sample billing records

### API Testing with Bruno

Bruno test collection is available in `bruno/usage-billing-service.bru`:
- Record usage events (API calls, storage, compute)
- Retrieve usage events
- Calculate and retrieve billing records
- Health checks and API documentation

To use Bruno:
1. Install [Bruno](https://www.usebruno.com/)
2. Open the `bruno/usage-billing-service.bru` file
3. Update variables (baseUrl, customerId, billingPeriod) if needed
4. Run requests

## Next Steps (Future Enhancements)

These can be added incrementally and controlled via feature flags:

- [ ] Docker containerization
- [ ] Docker Compose setup
- [ ] Kubernetes deployment manifests
- [ ] Prometheus metrics integration
- [ ] Grafana dashboards
- [ ] CI/CD pipeline (GitHub Actions)
- [ ] PostgreSQL support (currently H2)
- [ ] Advanced billing rules and pricing tiers
- [ ] Invoice generation
- [ ] Webhook notifications
- [ ] Advanced metrics and analytics

## Documentation

Comprehensive documentation is available in the [`docs/`](docs/) folder:

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Detailed architecture and design decisions
- **[USE_CASE.md](docs/USE_CASE.md)** - Use cases and API documentation
- **[DESIGN_DECISIONS.md](docs/DESIGN_DECISIONS.md)** - Design rationale and folder structure decisions
- **[EXTENSIONS.md](docs/EXTENSIONS.md)** - Future extensions (GraphQL, Kafka)
- **[INTEGRATION.md](docs/INTEGRATION.md)** - Apache Camel integration approach

## License

Apache 2.0
