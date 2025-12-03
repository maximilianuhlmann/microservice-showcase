# Docker Setup Guide

This guide explains how to run the Usage Billing Service using Docker and Docker Compose.

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- At least 4GB RAM available for containers
- Ports available: 8080 (app), 5432 (PostgreSQL), 9000 (SonarQube)

## Quick Start

### Using Makefile (Recommended)

The project includes a `Makefile` that simplifies Docker operations:

```bash
# Development environment (default)
make start          # Start all services
make stop           # Stop all services
make logs           # View logs (follow mode)
make ps             # Check service status
make rebuild        # Rebuild and restart after code changes
make reset          # Stop, remove volumes, and restart (clean slate)

# Production environment
make start-prod     # Start production services
make stop-prod      # Stop production services
make rebuild-prod   # Rebuild and restart production services
make reset-prod     # Stop, remove volumes, and restart production

# SonarQube analysis
make sonar-dev      # Run code quality analysis (development)
make sonar          # Run code quality analysis (production)

# Help
make help           # Show all available commands
```

### Manual Docker Compose Commands

If you prefer using `docker-compose` directly:

**Development:**
```bash
# Build and start all services
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env up -d

# View logs
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml logs -f billing-service

# Stop all services
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml down
```

**Production:**
```bash
# Build and start all services
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml --env-file prod.env up -d

# View logs
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml logs -f billing-service

# Stop all services
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml down
```

## Services

### 1. Usage Billing Service
- **Port**: 8080
- **URLs**:
  - API: http://localhost:8080/api/v1
  - Swagger UI: http://localhost:8080/swagger-ui.html
  - Togglz Console: http://localhost:8080/togglz (Basic Auth - see environment variables)
- **Health Check**: Checks Swagger UI endpoint
- **Profile**: `docker` (uses PostgreSQL)

### 2. PostgreSQL Database
- **Port**: 5433 (dev) or 5434 (prod) - exposed for external tools
- **Database**: `usagebillingdb` (configurable via `POSTGRES_DB`)
- **Username**: `usagebilling` (configurable via `POSTGRES_USER`)
- **Password**: Configure via `POSTGRES_PASSWORD` environment variable
- **Volume**: `postgres-data` (persistent storage)
- **Connection String**: `jdbc:postgresql://postgres:5432/usagebillingdb` (internal)

### 3. SonarQube (Code Quality)
- **Port**: 9000
- **URL**: http://localhost:9000
- **Authentication**: Anonymous access enabled (no token required for analysis)
- **Volumes**: 
  - `sonarqube-data` (data)
  - `sonarqube-extensions` (plugins)
  - `sonarqube-logs` (logs)
- **Database**: Separate PostgreSQL instance (`sonarqube-db`) with persistence

## Environment Variables

### Billing Service

| Variable | Default | Description |
|----------|---------|------------|
| `SPRING_PROFILES_ACTIVE` | `docker` | Spring profile |
| `DB_USERNAME` | `usagebilling` | PostgreSQL username |
| `DB_PASSWORD` | *(required)* | PostgreSQL password (set in `.env`) |
| `ADMIN_USERNAME` | `admin` | Admin console username |
| `ADMIN_PASSWORD` | *(required)* | Admin console password (set in `.env`) |
| `API_KEYS` | *(required)* | Comma-separated API keys (set in `.env`) |
| `BILLING_SCHEDULER_ENABLED` | `false` | Enable automatic billing |

### Required Environment Variables

**⚠️ IMPORTANT:** Docker Compose requires environment variables to be set. The project uses separate environment files for development and production:

- **Development**: `local.env` - For local development with less secure defaults
- **Production**: `prod.env` - For production with secure passwords

**Required variables (no defaults - must be set in environment files):**
- `POSTGRES_PASSWORD` - PostgreSQL database password
- `ADMIN_PASSWORD` - Admin console password
- `API_KEYS` - Comma-separated API keys
- `SONARQUBE_DB_PASSWORD` - SonarQube database password
- `SONARQUBE_ADMIN_PASSWORD` - SonarQube admin password

**Optional variables (have defaults):**
- `POSTGRES_DB` (default: `usagebillingdb`)
- `POSTGRES_USER` (default: `usagebilling`)
- `ADMIN_USERNAME` (default: `admin`)
- `BILLING_SCHEDULER_ENABLED` (default: `false`)

**⚠️ Security Note:** Never commit `local.env` or `prod.env` to version control. These files contain sensitive credentials.

## Database Access

### Using psql (from host)

```bash
# Development environment
docker exec -it usage-billing-service-postgres-dev psql -U usagebilling -d usagebillingdb

# Or using connection string (dev uses port 5433)
psql -h localhost -p 5433 -U usagebilling -d usagebillingdb

# Production environment (port 5434)
psql -h localhost -p 5434 -U usagebilling -d usagebillingdb
```

### Using External Tools

- **pgAdmin**: Connect to `localhost:5433` (dev) or `localhost:5434` (prod)
- **DBeaver**: Use connection string `jdbc:postgresql://localhost:5433/usagebillingdb` (dev) or `jdbc:postgresql://localhost:5434/usagebillingdb` (prod)
- **IntelliJ Database Tool**: Add PostgreSQL data source with appropriate port

**Note**: H2 console is disabled in Docker (using PostgreSQL). Use PostgreSQL client tools instead.

## Building the Image

```bash
# Using Makefile (recommended)
make build          # Build development images
make build-prod     # Build production images

# Manual build
docker build -t usage-billing-service:latest .

# Build with specific tag
docker build -t usage-billing-service:1.0.0 .
```

## Running Individual Services

### Start only database

```bash
# Development
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env up -d postgres

# Production
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml --env-file prod.env up -d postgres
```

### Start application and database (no SonarQube)

```bash
# Development
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env up -d postgres billing-service

# Production
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml --env-file prod.env up -d postgres billing-service
```

### Start SonarQube separately

```bash
# Development
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env up -d sonarqube sonarqube-db

# Production
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml --env-file prod.env up -d sonarqube sonarqube-db
```

## Volumes and Data Persistence

All data is persisted in Docker volumes:

- `postgres-data`: PostgreSQL database files
- `sonarqube-data`: SonarQube data
- `sonarqube-extensions`: SonarQube plugins
- `sonarqube-logs`: SonarQube logs
- `sonarqube-db-data`: SonarQube database

**Backup volumes:**
```bash
docker run --rm -v usage-billing-service_postgres-data:/data -v $(pwd):/backup alpine tar czf /backup/postgres-backup.tar.gz /data
```

**Restore volumes:**
```bash
docker run --rm -v usage-billing-service_postgres-data:/data -v $(pwd):/backup alpine tar xzf /backup/postgres-backup.tar.gz -C /
```

## Troubleshooting

### Application won't start

1. Check PostgreSQL is healthy:
   ```bash
   docker-compose ps postgres
   ```

2. Check application logs:
   ```bash
   docker-compose logs billing-service
   ```

3. Verify database connection:
   ```bash
   docker exec -it usage-billing-postgres psql -U usagebilling -d usagebillingdb -c "SELECT 1;"
   ```

### Port conflicts

If ports are already in use, modify the environment-specific compose files:

**Development** (`docker-compose.dev.yml`):
```yaml
ports:
  - "8081:8080"  # Change host port
```

**Production** (`docker-compose.prod.yml`):
```yaml
ports:
  - "8081:8080"  # Change host port
```

### Reset everything

```bash
# Using Makefile (recommended)
make reset          # Development
make reset-prod     # Production

# Manual reset
# Development
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env down -v
docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env up -d

# Production
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml --env-file prod.env down -v
docker-compose -f docker-compose.base.yml -f docker-compose.prod.yml --env-file prod.env up -d
```

## Production Considerations

1. **Set secure passwords** in `prod.env` file
2. **Use secrets management** (Docker secrets, Kubernetes secrets, etc.)
3. **Configure resource limits** in `docker-compose.prod.yml`:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1'
         memory: 1G
   ```
4. **Enable billing scheduler** for production (set `BILLING_SCHEDULER_ENABLED=true` in `prod.env`)
5. **Configure proper logging** (file-based, centralized)
6. **Set up monitoring** (Prometheus, Grafana)
7. **Use production-grade PostgreSQL** (managed service recommended)

## SonarQube Setup

1. Start SonarQube:
   ```bash
   make start          # Development (includes SonarQube)
   # Or start only SonarQube
   docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml --env-file local.env up -d sonarqube sonarqube-db
   ```

2. Wait for SonarQube to be ready (check logs):
   ```bash
   make logs           # View all logs
   # Or specifically SonarQube
   docker-compose -f docker-compose.base.yml -f docker-compose.dev.yml logs -f sonarqube
   ```

3. Access http://localhost:9000
4. **Anonymous access is enabled** - no login required for analysis
5. Run analysis:
   ```bash
   # Using Makefile (recommended)
   make sonar-dev      # Development
   make sonar          # Production

   # Manual
   mvn clean test sonar:sonar -Dsonar.host.url=http://localhost:9000
   ```

**Note:** SonarQube data persists across container restarts thanks to Docker volumes. The configuration uses anonymous access, so no token generation is required.

