# Bruno Collection Setup

## Environment Variables

This collection uses an `env.local` file for easy configuration. The `env.local` file is located at the root of this collection.

### Quick Setup (Recommended)

1. **Edit `env.local` file** with your values:
   ```
   BASE_URL=http://localhost:8080
   CUSTOMER_ID=customer-123
   BILLING_PERIOD=2025-11
   API_KEY=dev-api-key-123
   ```

2. **Import Environment in Bruno:**
   - Open the collection in Bruno
   - Click the environment dropdown (top toolbar, shows "No Environment")
   - Click "Configure environments" or the "+" icon
   - Click "Import Environment"
   - Select `environments/Local.json` file
   - The environment will be imported with all variables

   **OR** create manually:
   - Click "New Environment" or "+"
   - Name it "Local"
   - Add these variables:
     - `baseUrl`: `http://localhost:8080`
     - `customerId`: `customer-123`
     - `billingPeriod`: `2025-11`
     - `apiKey`: `dev-api-key-123`

3. **Select the "Local" environment** from the dropdown

### Alternative: Use Process Environment Variables Directly

You can also reference `.env` variables directly in requests using:
- `{{$processEnv.BASE_URL}}`
- `{{$processEnv.CUSTOMER_ID}}`
- `{{$processEnv.BILLING_PERIOD}}`
- `{{$processEnv.API_KEY}}`

This works without creating an environment file - Bruno reads the `.env` file automatically.

### Sync Script

If you prefer to edit the `env.local` file and sync to the Bruno environment:
```bash
./sync-env.sh
```

This will update `environments/Local.json` with values from `env.local`.

After syncing, re-import the JSON file in Bruno using "Import Environment" to update your environment.

