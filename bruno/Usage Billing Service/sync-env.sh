#!/bin/bash
# Script to sync env.local file values to Local.bru environment file

ENV_FILE="env.local"
JSON_ENV="environments/Local.json"

if [ ! -f "$ENV_FILE" ]; then
    echo "Error: $ENV_FILE not found"
    exit 1
fi

# Read env.local file and extract values
BASE_URL=$(grep "^BASE_URL=" "$ENV_FILE" | cut -d '=' -f2)
CUSTOMER_ID=$(grep "^CUSTOMER_ID=" "$ENV_FILE" | cut -d '=' -f2)
BILLING_PERIOD=$(grep "^BILLING_PERIOD=" "$ENV_FILE" | cut -d '=' -f2)
API_KEY=$(grep "^API_KEY=" "$ENV_FILE" | cut -d '=' -f2)

# Create JSON version for import
cat > "$JSON_ENV" << EOF
{
  "name": "Local",
  "variables": [
    {
      "name": "baseUrl",
      "value": "${BASE_URL}"
    },
    {
      "name": "customerId",
      "value": "${CUSTOMER_ID}"
    },
    {
      "name": "billingPeriod",
      "value": "${BILLING_PERIOD}"
    },
    {
      "name": "apiKey",
      "value": "${API_KEY}"
    }
  ]
}
EOF

echo "✅ Synced env.local values to $JSON_ENV"
echo "   baseUrl: $BASE_URL"
echo "   customerId: $CUSTOMER_ID"
echo "   billingPeriod: $BILLING_PERIOD"
echo "   apiKey: $API_KEY"
echo ""
echo "📥 To import in Bruno: Use 'Import Environment' and select $JSON_ENV"

