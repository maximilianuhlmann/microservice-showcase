# Togglz Admin Console Guide

## Accessing the Console

Once the application is running, access the Togglz admin console at:

**URL:** `http://localhost:8080/togglz`

**Authentication Required:** The console is protected with Basic Authentication.

**Default Credentials:**
- **Username:** `admin`
- **Password:** `admin123`

When you access the URL, your browser will prompt for credentials. Enter the username and password above.

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

# Admin Authentication
admin.username=admin
admin.password=admin123
```

**Security:** The console is protected by Spring Security with Basic Authentication. Change the default credentials in production!

## Using the Console

1. Start the application: `mvn spring-boot:run`
2. Open browser: `http://localhost:8080/togglz`
3. Enter credentials when prompted:
   - Username: `admin`
   - Password: `admin123`
4. You'll see all features from `Features` enum:
   - Real-time Billing
   - Usage Aggregation
   - Invoice Generation
   - Webhook Notifications
   - Advanced Metrics
5. Click the toggle switch to enable/disable features
6. Changes take effect immediately (no restart needed)

## Changing Credentials

To change the admin credentials, update `application.properties`:

```properties
admin.username=your-username
admin.password=your-secure-password
```

**Important:** For production, use strong passwords and consider using environment variables or a secrets manager instead of hardcoding credentials.


