# Quick Start Guide

## Running the Application

```bash
# Option 1: Using Maven
mvn spring-boot:run

# Option 2: Build and run JAR
mvn clean package
java -jar target/usage-billing-service-1.0.0-SNAPSHOT.jar
```

The application starts on **http://localhost:8080**

## Accessing OpenAPI/Swagger Documentation

Once the application is running, access the API documentation at:

### Swagger UI (Interactive)
**URL:** `http://localhost:8080/swagger-ui.html`

This provides an interactive interface where you can:
- Browse all API endpoints
- See request/response schemas
- Test endpoints directly from the browser
- View example requests and responses

### OpenAPI JSON
**URL:** `http://localhost:8080/api-docs`

This returns the OpenAPI specification in JSON format, useful for:
- API client generation
- Importing into API testing tools (Postman, Insomnia)
- Documentation tools

## Accessing Togglz Admin Console

**URL:** `http://localhost:8080/togglz`

**Authentication Required:** Basic Auth
- Credentials configured in `application.properties` (`admin.username` and `admin.password`)
- Default development credentials: see `application.properties` (change in production!)

Manage feature flags in real-time without restarting the application.

## About `.java21.env`

The `.java21.env` file is a convenience script to set `JAVA_HOME` to Java 21 for this project.

**Note:** This file is only needed if your system's default Java version is not Java 21. If you have multiple Java versions installed, use this file to ensure the project uses Java 21.

### Usage

```bash
# Source the file before running Maven commands
source .java21.env
mvn clean test

# Or export directly
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
mvn spring-boot:run
```

### When to use it

- Your system's default Java is not Java 21
- You have multiple Java versions and want to switch for this project
- CI/CD pipelines need explicit Java version configuration

### When NOT to use it

- Java 21 is already your default `JAVA_HOME`
- You're using a Java version manager (jenv, SDKMAN, etc.) that handles versions automatically

**Tip:** Check your Java version with `java -version` and `echo $JAVA_HOME` to see if you need this file.

## Quick Links

| Service | URL | Authentication |
|---------|-----|----------------|
| **Swagger UI** | http://localhost:8080/swagger-ui.html | None (public) |
| **OpenAPI JSON** | http://localhost:8080/api-docs | None (public) |
| **Togglz Console** | http://localhost:8080/togglz | Basic Auth (see `application.properties`) |
| **H2 Console** | http://localhost:8080/h2-console | Basic Auth (see `application.properties`) |
| **Health Check** | http://localhost:8080/actuator/health | Application health status |


