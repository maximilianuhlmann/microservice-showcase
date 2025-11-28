# Multi-stage build for Spring Boot application

# Stage 1: Build
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

# Copy pom.xml first for better layer caching
COPY pom.xml .

# Copy source code and build (dependencies will be downloaded during package)
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Runtime
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Install wget for health checks and create non-root user for security
RUN apk add --no-cache wget && \
    addgroup -S spring && \
    adduser -S spring -G spring
USER spring:spring

# Copy JAR from build stage
COPY --from=build /app/target/usage-billing-service-*.jar app.jar

# Expose application port
EXPOSE 8080

# Health check - check if Swagger UI is accessible
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/swagger-ui.html || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

