#!/bin/bash
# Practical script to organize changes into logical commits on one feature branch

set -e

echo "📦 Organizing changes into logical commits..."
echo ""

# Ensure we're on main
git checkout main

# Create feature branch
echo "🌿 Creating feature branch..."
git checkout -b feature/security-billing-enhancements

# Apply all stashed changes
echo "📥 Applying stashed changes..."
git stash pop

echo ""
echo "✅ All changes applied to feature/security-billing-enhancements"
echo ""
echo "📝 Next steps - commit in logical groups:"
echo ""
echo "1. Database models and migrations:"
echo "   git add src/main/java/com/microservice/billing/domain/{Customer,ApiKey,PricingRate,DefaultRate,BillingBreakdown}.java"
echo "   git add src/main/java/com/microservice/billing/repository/{Customer,ApiKey,PricingRate,DefaultRate,BillingBreakdown}Repository.java"
echo "   git add src/main/resources/db/migration/V*.sql"
echo "   git commit -m 'feat: Add database models for customers, API keys, pricing, and billing breakdown'"
echo ""
echo "2. API Key Authentication:"
echo "   git add src/main/java/com/microservice/billing/config/ApiKey*.java"
echo "   git add src/main/java/com/microservice/billing/config/SecurityConfig.java"
echo "   git commit -m 'feat: Implement API key authentication for REST API'"
echo ""
echo "3. Customer isolation and access control:"
echo "   git add src/main/java/com/microservice/billing/{config/ApiKeyCustomerMapper,service/CustomerContextService}.java"
echo "   git add src/main/java/com/microservice/billing/controller/{BillingController,UsageEventController}.java"
echo "   git commit -m 'feat: Add customer isolation and access control'"
echo ""
echo "4. Pricing service:"
echo "   git add src/main/java/com/microservice/billing/service/PricingService.java"
echo "   git add src/main/java/com/microservice/billing/service/BillingService.java"
echo "   git commit -m 'feat: Implement customer-specific pricing service'"
echo ""
echo "5. Billing breakdown and format changes:"
echo "   git add src/main/java/com/microservice/billing/domain/BillingRecord.java"
echo "   git add src/main/java/com/microservice/billing/controller/BillingBreakdownDto.java"
echo "   git add src/main/java/com/microservice/billing/mapper/BillingRecordMapper.java"
echo "   git add src/main/java/com/microservice/billing/controller/BillingRecordDto.java"
echo "   git commit -m 'feat: Add billing breakdown and change period format to YYYY-MM'"
echo ""
echo "6. Custom exceptions:"
echo "   git add src/main/java/com/microservice/billing/exception/"
echo "   git add src/main/java/com/microservice/billing/controller/GlobalExceptionHandler.java"
echo "   git commit -m 'feat: Replace standard exceptions with domain-specific custom exceptions'"
echo ""
echo "7. Billing scheduler:"
echo "   git add src/main/java/com/microservice/billing/service/BillingScheduler.java"
echo "   git add src/main/java/com/microservice/billing/UsageBillingApp.java"
echo "   git commit -m 'feat: Add scheduled billing calculation'"
echo ""
echo "8. Tests:"
echo "   git add src/test/java/com/microservice/billing/**/*.java"
echo "   git commit -m 'test: Add comprehensive test coverage for new features'"
echo ""
echo "9. Bruno collection:"
echo "   git add bruno/ .gitignore"
echo "   git commit -m 'feat: Add Bruno API collection with environment setup'"
echo ""
echo "10. Documentation:"
echo "    git add docs/"
echo "    git commit -m 'docs: Update architecture and use case documentation'"
echo ""

