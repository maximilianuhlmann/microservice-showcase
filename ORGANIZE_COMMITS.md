# Commit Organization Plan

This document outlines how to organize the current changes into logical branches and commits.

## Current State
All changes are stashed. We'll create feature branches from main and apply changes in logical groups.

## Proposed Branch Structure

### 1. `feature/api-key-authentication`
**Purpose:** Implement API key authentication for REST API endpoints

**Files:**
- `src/main/java/com/microservice/billing/config/ApiKeyAuthenticationFilter.java`
- `src/main/java/com/microservice/billing/config/ApiKeyAuthenticationProvider.java`
- `src/main/java/com/microservice/billing/config/ApiKeyAuthenticationToken.java`
- `src/main/java/com/microservice/billing/config/ApiKeyAuthenticationException.java`
- `src/main/java/com/microservice/billing/config/ApiKeyValidator.java`
- `src/main/java/com/microservice/billing/config/SecurityConfig.java` (API key parts)
- `src/test/java/com/microservice/billing/config/ApiKeyAuthenticationFilterTest.java`
- `src/test/java/com/microservice/billing/config/ApiKeyAuthenticationProviderTest.java`

### 2. `feature/customer-isolation`
**Purpose:** Implement customer isolation and access control

**Files:**
- `src/main/java/com/microservice/billing/config/ApiKeyCustomerMapper.java`
- `src/main/java/com/microservice/billing/service/CustomerContextService.java`
- `src/main/java/com/microservice/billing/controller/BillingController.java` (access control)
- `src/main/java/com/microservice/billing/controller/UsageEventController.java` (access control)
- `src/test/java/com/microservice/billing/config/ApiKeyCustomerMapperTest.java`
- `src/test/java/com/microservice/billing/service/CustomerContextServiceTest.java`
- `src/test/java/com/microservice/billing/controller/BillingControllerSecurityTest.java`

### 3. `feature/database-models-customer-pricing`
**Purpose:** Add database models for customers, API keys, and pricing

**Files:**
- `src/main/java/com/microservice/billing/domain/Customer.java`
- `src/main/java/com/microservice/billing/domain/ApiKey.java`
- `src/main/java/com/microservice/billing/domain/PricingRate.java`
- `src/main/java/com/microservice/billing/domain/DefaultRate.java`
- `src/main/java/com/microservice/billing/repository/CustomerRepository.java`
- `src/main/java/com/microservice/billing/repository/ApiKeyRepository.java`
- `src/main/java/com/microservice/billing/repository/PricingRateRepository.java`
- `src/main/java/com/microservice/billing/repository/DefaultRateRepository.java`
- `src/main/resources/db/migration/V2__Create_customer_and_pricing_tables.sql`
- `src/main/resources/db/migration/V3__Add_default_rates_and_billing_breakdown.sql`
- `src/main/resources/db/migration/V4__Insert_dev_customer_and_api_key.sql`
- `src/test/java/com/microservice/billing/repository/PricingRateRepositoryTest.java`
- `src/test/java/com/microservice/billing/repository/DefaultRateRepositoryTest.java`

### 4. `feature/pricing-service`
**Purpose:** Implement customer-specific pricing service

**Files:**
- `src/main/java/com/microservice/billing/service/PricingService.java`
- `src/main/java/com/microservice/billing/service/BillingService.java` (pricing integration)
- `src/test/java/com/microservice/billing/service/PricingServiceTest.java`
- `src/test/java/com/microservice/billing/service/BillingServiceTest.java` (pricing updates)

### 5. `feature/billing-breakdown`
**Purpose:** Add billing breakdown by service type

**Files:**
- `src/main/java/com/microservice/billing/domain/BillingBreakdown.java`
- `src/main/java/com/microservice/billing/controller/BillingBreakdownDto.java`
- `src/main/java/com/microservice/billing/repository/BillingBreakdownRepository.java`
- `src/main/java/com/microservice/billing/mapper/BillingRecordMapper.java` (breakdown mapping)
- `src/main/java/com/microservice/billing/controller/BillingRecordDto.java` (breakdown field)
- `src/main/java/com/microservice/billing/service/BillingService.java` (breakdown logic)
- `src/test/java/com/microservice/billing/service/BillingServiceBreakdownTest.java`
- `src/test/java/com/microservice/billing/controller/BillingControllerBreakdownTest.java`

### 6. `feature/custom-exceptions`
**Purpose:** Replace standard exceptions with domain-specific custom exceptions

**Files:**
- `src/main/java/com/microservice/billing/exception/CustomerAccessDeniedException.java`
- `src/main/java/com/microservice/billing/exception/FeatureDisabledException.java`
- `src/main/java/com/microservice/billing/exception/DomainValidationException.java`
- `src/main/java/com/microservice/billing/service/CustomerContextService.java` (exception usage)
- `src/main/java/com/microservice/billing/service/BillingService.java` (exception usage)
- `src/main/java/com/microservice/billing/domain/BillingRecord.java` (exception usage)
- `src/main/java/com/microservice/billing/controller/GlobalExceptionHandler.java` (exception handlers)
- `src/test/java/com/microservice/billing/controller/GlobalExceptionHandlerTest.java` (exception tests)

### 7. `feature/billing-scheduler`
**Purpose:** Add scheduled billing calculation

**Files:**
- `src/main/java/com/microservice/billing/service/BillingScheduler.java`
- `src/main/java/com/microservice/billing/UsageBillingApp.java` (@EnableScheduling)
- `src/main/resources/application.properties` (scheduler config)
- `src/test/java/com/microservice/billing/service/BillingSchedulerTest.java`

### 8. `feature/billing-period-format`
**Purpose:** Change billing period from LocalDate to YYYY-MM string format

**Files:**
- `src/main/java/com/microservice/billing/domain/BillingRecord.java` (billingPeriod format)
- `src/main/java/com/microservice/billing/controller/BillingController.java` (YearMonth parsing)
- `src/main/java/com/microservice/billing/controller/BillingRecordDto.java` (billingPeriod format)
- `src/main/java/com/microservice/billing/service/BillingService.java` (YearMonth usage)
- `src/main/resources/db/migration/V3__Add_default_rates_and_billing_breakdown.sql` (ALTER COLUMN)
- `src/test/java/com/microservice/billing/domain/BillingRecordTest.java`
- `src/test/java/com/microservice/billing/repository/BillingRecordRepositoryTest.java`
- `src/test/java/com/microservice/billing/service/BillingServiceTest.java`
- `src/test/java/com/microservice/billing/controller/BillingControllerTest.java`
- `src/test/java/com/microservice/billing/integration/stepdefs/BillingStepDefinitions.java`

### 9. `feature/exception-handler-improvements`
**Purpose:** Improve global exception handling

**Files:**
- `src/main/java/com/microservice/billing/controller/GlobalExceptionHandler.java` (DateTimeParseException, SecurityException)
- `src/test/java/com/microservice/billing/controller/GlobalExceptionHandlerTest.java`

### 10. `feature/bruno-collection`
**Purpose:** Add Bruno API collection with environment setup

**Files:**
- `bruno/Usage Billing Service/` (entire directory)
- `bruno/usage-billing-service.bru` (deleted)
- `.gitignore` (Bruno .env ignore)

### 11. `chore/update-docs`
**Purpose:** Update documentation

**Files:**
- `docs/ARCHITECTURE.md`
- `docs/USE_CASE.md`

## Execution Order

1. Create branches in dependency order (database models first, then services that use them)
2. Apply relevant changes from stash to each branch
3. Commit each branch with descriptive messages
4. Merge branches back to main in logical order

## Commands to Execute

See the script below or execute manually branch by branch.

