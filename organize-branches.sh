#!/bin/bash
# Script to organize stashed changes into logical feature branches
# Run this script to create branches and apply changes in logical groups

set -e

echo "📦 Organizing changes into feature branches..."
echo ""

# Ensure we're on main and clean
git checkout main
git status

# Create branches in dependency order
echo "🌿 Creating feature branches..."

# 1. Database models first (foundation)
git checkout -b feature/database-models-customer-pricing
git stash pop
git add src/main/java/com/microservice/billing/domain/Customer.java
git add src/main/java/com/microservice/billing/domain/ApiKey.java
git add src/main/java/com/microservice/billing/domain/PricingRate.java
git add src/main/java/com/microservice/billing/domain/DefaultRate.java
git add src/main/java/com/microservice/billing/repository/CustomerRepository.java
git add src/main/java/com/microservice/billing/repository/ApiKeyRepository.java
git add src/main/java/com/microservice/billing/repository/PricingRateRepository.java
git add src/main/java/com/microservice/billing/repository/DefaultRateRepository.java
git add src/main/resources/db/migration/V2__Create_customer_and_pricing_tables.sql
git add src/main/resources/db/migration/V3__Add_default_rates_and_billing_breakdown.sql
git add src/main/resources/db/migration/V4__Insert_dev_customer_and_api_key.sql
git add src/test/java/com/microservice/billing/repository/PricingRateRepositoryTest.java
git add src/test/java/com/microservice/billing/repository/DefaultRateRepositoryTest.java
git commit -m "feat: Add database models for customers, API keys, and pricing

- Add Customer, ApiKey, PricingRate, and DefaultRate entities
- Add corresponding repositories
- Add Flyway migrations for new tables
- Add default rates and billing breakdown table
- Add test coverage for repositories"

echo "✅ Created feature/database-models-customer-pricing"

# Continue with other branches...
# Note: This is a template - you'll need to manually organize the remaining changes
# or continue building out this script

echo ""
echo "⚠️  This script is a template. You'll need to:"
echo "   1. Continue adding branches for remaining features"
echo "   2. Or manually create branches and apply changes from stash"
echo ""
echo "Current stash:"
git stash list

