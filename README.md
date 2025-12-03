# Usage Billing Service - MVP

A minimal viable product (MVP) for usage-based billing built with Java Spring Boot, following **TDD principles** and **MVC architecture**.

## Technology Stack

- **Java 21** (LTS)
- **Spring Boot 3.2.0**
- **Spring Data JPA** (Hibernate)
- **H2 Database** (in-memory for development)
- **PostgreSQL** (for Docker/production)
- **Docker** (containerization)
- **Maven** (build tool)
- **Lombok** (reducing boilerplate)
- **JUnit 5** (for testing)
- **Testcontainers** (for integration tests)
- **Togglz** (feature flags)
- **SpringDoc OpenAPI** (API documentation)

## Architecture

Built using **MVC (Model-View-Controller)** pattern:

- **Model**: Domain entities (`UsageEvent`, `BillingRecord`)
- **View**: REST API with DTOs
- **Controller**: REST controllers (`UsageEventController`, `BillingController`)
- **Service**: Business logic layer
- **Repository**: Data access layer

## Features

### MVP Features

1. ✅ **Record Usage Events**: Track usage events (customer, service type, quantity, unit)
2. ✅ **Retrieve Usage Events**: Get usage events by customer and/or service type
3. ✅ **Calculate Billing**: Aggregate usage and calculate billing amounts with detailed breakdown by service type
4. ✅ **Automatic Billing**: Scheduled monthly billing calculation for all active customers
5. ✅ **Database-Driven Pricing**: Default rates stored in database with optional customer-specific overrides
6. ✅ **API Key Authentication**: Secure API access with customer context isolation
7. ✅ **Feature Flags**: Togglz-based feature flags for toggling features on/off
8. ✅ **OpenAPI Documentation**: Swagger UI for API documentation

### Feature Flags (Togglz)

**Admin Console:** Access at `http://localhost:8080/togglz` to manage feature flags in real-time.

Configured in `application.properties`:
- `togglz.usage-aggregation=true` - Enable/disable usage aggregation
- `togglz.realtime-billing=false` - Enable real-time billing (future)
- `togglz.invoice-generation=false` - Enable invoice generation (future)
- `togglz.webhook-notifications=false` - Enable webhooks (future)
- `togglz.advanced-metrics=false` - Enable advanced metrics (future)

Usage in code:
```java
@RequiredArgsConstructor
public class YourService {
    private final FeatureManager featureManager;
    
    public void someMethod() {
        if (featureManager.isActive(Features.USAGE_AGGREGATION)) {
            // Feature is enabled
        }
    }
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
│   │   │   └── OpenApiConfig.java    # OpenAPI/Swagger config
│   │   ├── domain/
│   │   │   ├── UsageEvent.java
│   │   │   └── BillingRecord.java
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

### Accessing API Documentation

- **Swagger UI (Interactive):** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs
- **Togglz Admin Console:** http://localhost:8080/togglz (Basic Auth - see `application.properties`)
- **H2 Console:** http://localhost:8080/h2-console (Basic Auth - see `application.properties`)

**⚠️ Security Note:** Default credentials are for development only. Change them in production using environment variables.

### Running with Docker

The application can be run using Docker Compose with PostgreSQL and SonarQube:

```bash
# Development environment (using Makefile)
make start          # Start all services
make stop           # Stop all services
make logs           # View logs
make ps             # Check service status
make rebuild        # Rebuild and restart after code changes
make reset          # Stop, remove volumes, and restart (clean slate)

# Production environment
make start-prod     # Start production services
make stop-prod      # Stop production services
make rebuild-prod   # Rebuild and restart production services

# SonarQube analysis
make sonar-dev      # Run code quality analysis (development)
```

**Services:**
- **Billing Service**: http://localhost:8080
- **PostgreSQL**: Port 5433 (dev) or 5434 (prod)
- **SonarQube**: http://localhost:9000 (anonymous access enabled)

**Environment Files:**
- `local.env` - Development environment variables
- `prod.env` - Production environment variables

See [DOCKER.md](docs/DOCKER.md) for detailed Docker setup instructions.

### Running Tests

```bash
# Run all tests (unit + integration)
mvn test

# Run only unit tests (excludes Cucumber integration tests)
mvn test -Dtest='*Test'

# Run only Cucumber integration tests
mvn test -Dtest='RunCucumberTest'
```

### Code Quality & Test Coverage

#### JaCoCo Test Coverage

JaCoCo automatically generates coverage reports when running tests:

```bash
# Run tests and generate coverage report
mvn clean test

# View HTML coverage report
open target/site/jacoco/index.html
```

**Coverage Reports:**
- **HTML Report**: `target/site/jacoco/index.html` - Interactive browser report
- **XML Report**: `target/site/jacoco/jacoco.xml` - For SonarQube integration

#### SonarQube Analysis

Run SonarQube analysis (requires SonarQube server):

```bash
# Run SonarQube analysis
mvn sonar:sonar

# Or with explicit server configuration
mvn sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=your-token
```

**Note:** SonarQube automatically reads JaCoCo XML reports for coverage metrics. You need both:
- **JaCoCo** generates the coverage data
- **SonarQube** displays it alongside code quality metrics (bugs, vulnerabilities, code smells)

#### SonarLint (IDE Plugin)

For real-time code quality feedback in your IDE:
- **IntelliJ IDEA**: Settings → Plugins → Install "SonarLint"
- **VS Code**: Extensions → Install "SonarLint"
- **Eclipse**: Marketplace → Install "SonarLint"

SonarLint works automatically as you code and doesn't require a SonarQube server.

## API Endpoints

### Usage Events

- **POST** `/api/v1/usage-events` - Record a usage event
- **GET** `/api/v1/usage-events/customer/{customerId}` - Get usage events for a customer
- **GET** `/api/v1/usage-events/customer/{customerId}/service/{serviceType}` - Get usage events filtered by service type

### Billing

- **POST** `/api/v1/billing/{customerId}/calculate?billingPeriod=2024-01` - Calculate billing (format: YYYY-MM)
- **GET** `/api/v1/billing/{customerId}?billingPeriod=2024-01` - Get billing record with breakdown (format: YYYY-MM)

### API Documentation

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`

### Authentication

All `/api/**` endpoints require API key authentication via the `X-API-Key` header:

```bash
curl -X GET http://localhost:8080/api/v1/billing/customer-123?billingPeriod=2024-01 \
  -H "X-API-Key: dev-api-key-123"
```

**API Key Configuration:**
- API keys are stored in the `api_keys` table
- Each API key is associated with a customer, ensuring customers can only access their own data
- For development, see `application.properties` for default API key configuration
- **⚠️ Security Note:** Use strong, unique API keys in production. Never commit API keys to version control.

### Example Requests

**Record Usage Event:**
```bash
curl -X POST http://localhost:8080/api/v1/usage-events \
  -H "Content-Type: application/json" \
  -H "X-API-Key: YOUR_API_KEY" \
  -d '{
    "customerId": "customer-123",
    "serviceType": "api-calls",
    "quantity": 10.5,
    "unit": "requests"
  }'
```

**Get Usage Events:**
```bash
curl http://localhost:8080/api/v1/usage-events/customer/YOUR_CUSTOMER_ID \
  -H "X-API-Key: YOUR_API_KEY"
```

**Calculate Billing:**
```bash
curl -X POST "http://localhost:8080/api/v1/billing/YOUR_CUSTOMER_ID/calculate?billingPeriod=2024-01" \
  -H "X-API-Key: YOUR_API_KEY"
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
- Sample usage events for customer-123 and customer-456
- Sample billing records

### API Testing with Bruno

Bruno test collection is available in `bruno/Usage Billing Service/`:
- Record usage events (API calls, storage, compute)
- Retrieve usage events
- Calculate and retrieve billing records with breakdown
- Error handling tests (unauthorized access, invalid formats)
- Health checks and API documentation

To use Bruno:
1. Install [Bruno](https://www.usebruno.com/)
2. Import the collection from `bruno/Usage Billing Service/`
3. Import the environment from `bruno/Usage Billing Service/environments/Local.json`
4. Update variables in `bruno/Usage Billing Service/env.local` if needed
5. Run requests

See `bruno/Usage Billing Service/README.md` for detailed setup instructions.

## Next Steps (Future Enhancements)

These can be added incrementally and controlled via feature flags:

- [x] Docker containerization
- [x] Docker Compose setup
- [ ] Kubernetes deployment manifests
- [ ] Prometheus metrics integration
- [ ] Grafana dashboards
- [ ] CI/CD pipeline (GitHub Actions)
- [x] PostgreSQL support (for Docker/production)
- [ ] Advanced billing rules and pricing tiers
- [ ] Invoice generation
- [ ] Webhook notifications
- [ ] Advanced metrics and analytics

## Documentation

Comprehensive documentation is available in the [`docs/`](docs/) folder:

- **[ARCHITECTURE.md](docs/ARCHITECTURE.md)** - Detailed architecture and design decisions
- **[USE_CASE.md](docs/USE_CASE.md)** - Use cases and API documentation
- **[DESIGN_DECISIONS.md](docs/DESIGN_DECISIONS.md)** - Design rationale and folder structure decisions
- **[DOCKER.md](docs/DOCKER.md)** - Docker setup and deployment guide
- **[QUICK_START.md](docs/QUICK_START.md)** - Quick start guide
- **[TOGGLZ_CONSOLE.md](docs/TOGGLZ_CONSOLE.md)** - Togglz feature flags console guide
- **[EXTENSIONS.md](docs/EXTENSIONS.md)** - Future extensions (GraphQL, Kafka)
- **[INTEGRATION.md](docs/INTEGRATION.md)** - Apache Camel integration approach

## License

Apache 2.0
