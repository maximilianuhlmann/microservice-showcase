# Usage-Based Billing Service - Use Case

## Overview

This microservice provides **usage-based billing** functionality for tracking, aggregating, and calculating charges based on customer usage of various services (e.g., API calls, storage, compute time, data transfer).

## Problem Statement

In modern SaaS and cloud platforms, customers are charged based on their actual usage rather than fixed subscription fees. This requires:
- **Real-time tracking** of usage events
- **Accurate aggregation** of usage data
- **Flexible billing calculation** based on usage patterns
- **Audit trail** for billing disputes

## Use Cases

### 1. Recording Usage Events

**Actor:** External services (API gateway, storage service, compute service)

**Flow:**
1. A customer performs an action that generates usage (e.g., makes an API call, stores data)
2. The external service calls `POST /api/v1/usage-events` with usage details
3. The service stores the usage event for later aggregation

**Example:**
```json
POST /api/v1/usage-events
{
  "customerId": "customer-123",
  "serviceType": "api-calls",
  "quantity": 1000,
  "unit": "requests",
  "timestamp": "2024-01-15T10:30:00",
  "metadata": "{\"endpoint\":\"/api/users\", \"method\":\"GET\"}"
}
```

### 2. Calculating Billing

**Actor:** Billing system, scheduled job (automatic), or manual trigger

**Flow:**
1. **Automatic (Scheduled)**: The `BillingScheduler` automatically calculates billing for all active customers on the first day of each month for the previous month
2. **Manual**: At any time, the system can call `POST /api/v1/billing/{customerId}/calculate`
3. The service aggregates all usage events for that period
4. Applies pricing rules from database (default rates or customer-specific rates)
5. Creates a billing record with detailed breakdown by service type

**Example:**
```
POST /api/v1/billing/customer-123/calculate?billingPeriod=2024-01
```

**Response:**
```json
{
  "id": 1,
  "customerId": "customer-123",
  "billingPeriod": "2024-01",
  "totalAmount": 10.50,
  "breakdown": [
    {
      "serviceType": "api-calls",
      "quantity": 1000,
      "rate": 0.001,
      "amount": 1.00
    },
    {
      "serviceType": "storage",
      "quantity": 50,
      "rate": 0.10,
      "amount": 5.00
    }
  ],
  "createdAt": "2024-02-01T00:00:00"
}
```

### 3. Retrieving Usage Data

**Actor:** Customer portal, billing team, support team

**Flow:**
1. Query usage events by customer: `GET /api/v1/usage-events/customer/{customerId}`
2. Filter by service type: `GET /api/v1/usage-events/customer/{customerId}/service/{serviceType}`
3. Get aggregated usage: `GET /api/v1/billing/{customerId}/usage-by-service`
4. Get total usage: `GET /api/v1/billing/{customerId}/total-usage`

## Data Flow

```mermaid
flowchart LR
    A["External Service: API Gateway"] -->|POST /api/v1/usage-events| B["Usage Billing Service"]
    
    B1["1. Store event"] --> B2["2. Aggregate"]
    B2 --> B3["3. Calculate"]
    
    B --> B1
    B3 -->|GET /api/v1/billing/{customerId}| C["Billing System: Invoice Generation"]
    
    style A fill:#e1f5ff
    style B fill:#fff4e1
    style C fill:#e1ffe1
```

## Service Types

The service supports multiple service types for different usage metrics:

- **api-calls**: API request count
- **storage**: Data storage in GB
- **compute**: Compute time in hours
- **data-transfer**: Data transfer in GB
- **custom**: Any custom metric

## Billing Model

Currently implemented as a **database-driven rate per unit** model:
- **Default rates** stored in `default_rates` table (per service type)
- **Customer-specific rates** can be configured in `pricing_rates` table (optional)
- **Breakdown included** - billing responses include detailed breakdown by service type
- **Calculation**: Total = Sum of (quantity × rate) for each service type

**Default Rates:**
- `api-calls`: $0.001 per request
- `storage`: $0.10 per GB
- `compute`: $0.50 per hour
- `data-transfer`: $0.05 per GB

**Billing Breakdown:**
Each billing record includes a breakdown showing:
- Service type
- Total quantity used
- Rate applied
- Amount charged

**Future enhancements:**
- Tiered pricing (volume discounts)
- Time-based pricing
- Custom pricing rules

## REST API Endpoints

### Usage Events

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/usage-events` | Record a usage event |
| GET | `/api/v1/usage-events/customer/{customerId}` | Get all usage events for a customer |
| GET | `/api/v1/usage-events/customer/{customerId}/service/{serviceType}` | Get usage events filtered by service type |

### Billing

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/billing/{customerId}/calculate?billingPeriod=YYYY-MM` | Calculate billing for a period (format: YYYY-MM, e.g., "2024-01") |
| GET | `/api/v1/billing/{customerId}?billingPeriod=YYYY-MM` | Get billing record with breakdown (format: YYYY-MM) |
| GET | `/api/v1/billing/{customerId}/usage-by-service` | Get aggregated usage by service type |

## Technical Highlights

### Stream Operations

The service demonstrates proficiency with Java streams:

1. **Mapping entities to DTOs:**
   ```java
   events.stream()
       .map(this::mapToDto)
       .collect(Collectors.toList())
   ```

2. **Aggregating quantities:**
   ```java
   usageEvents.stream()
       .map(UsageEvent::getQuantity)
       .reduce(BigDecimal.ZERO, BigDecimal::add)
   ```

3. **Grouping by service type:**
   ```java
   events.stream()
       .collect(Collectors.groupingBy(
           UsageEvent::getServiceType,
           Collectors.reducing(BigDecimal.ZERO, UsageEvent::getQuantity, BigDecimal::add)
       ))
   ```

### Database Migrations

Uses **Flyway** for version-controlled database schema:
- `V1__Create_initial_tables.sql` - PostgreSQL (used for both development and production)

### Mapper Pattern

Explicit mapper classes following the pattern seen in your codebase:
- `UsageEventMapper` - Maps between `UsageEvent` entity and `UsageEventDto`
- `BillingRecordMapper` - Maps between `BillingRecord` entity and `BillingRecordDto`

## Future Enhancements

- **Batch processing** for high-volume usage events
- **Real-time billing** calculation (feature flag controlled)
- **Invoice generation** (feature flag controlled)
- **Webhook notifications** for billing events
- **Advanced metrics** and analytics
- **Multi-currency** support
- **Tax calculation**
- **Discounts and promotions**

