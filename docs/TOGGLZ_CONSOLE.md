# Togglz Admin Console Guide

## Accessing the Console

Once the application is running, access the Togglz admin console at:

**URL:** `http://localhost:8080/togglz`

**Authentication Required:** The console is protected with Basic Authentication.

**Authentication Required:** The console uses Basic Authentication.

**Credentials:** Configure in `application.properties` or via environment variables:
- `admin.username` (default: `admin`)
- `admin.password` (default: `admin123` for development only)

**⚠️ Security Note:** Change default credentials in production! Use environment variables or a secrets manager.

## Features

The Togglz console allows you to:
- View all feature flags and their current state
- Toggle features on/off in real-time
- See feature descriptions and labels
- Manage feature states without restarting the application

## Configuration

The console is configured in `application.properties`:

```properties
# Togglz Console (Admin UI) - Protected with basic auth
togglz.console.enabled=true
togglz.console.path=/togglz
togglz.console.secured=true

# Admin Authentication (use environment variables in production)
admin.username=${ADMIN_USERNAME:admin}
admin.password=${ADMIN_PASSWORD:admin123}
```

**Security:** The console is protected by Spring Security with Basic Authentication. Change the default credentials in production!

## Using the Console

1. Start the application: `mvn spring-boot:run`
2. Open browser: `http://localhost:8080/togglz`
3. Enter credentials when prompted (configured in `application.properties` or environment variables)
4. You'll see all features from `Features` enum:
   - Real-time Billing
   - Usage Aggregation
   - Invoice Generation
   - Webhook Notifications
   - Advanced Metrics
5. Click the toggle switch to enable/disable features
6. Changes take effect immediately (no restart needed)

## Changing Credentials

### Option 1: Environment Variables (Recommended for Production)

```bash
export ADMIN_USERNAME=your-username
export ADMIN_PASSWORD=your-secure-password
mvn spring-boot:run
```

### Option 2: application.properties (Development Only)

```properties
admin.username=your-username
admin.password=your-secure-password
```

**Important:** 
- Never commit credentials to version control
- Use environment variables or a secrets manager in production
- See `.env.example` for a template of required variables


