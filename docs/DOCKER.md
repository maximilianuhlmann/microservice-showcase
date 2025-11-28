# Docker Setup Guide

This guide explains how to run the Usage Billing Service using Docker and Docker Compose.

## Prerequisites

- Docker Desktop (or Docker Engine + Docker Compose)
- At least 4GB RAM available for containers
- Ports available: 8080 (app), 5432 (PostgreSQL), 9000 (SonarQube)

## Quick Start

### Production-like Setup

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f billing-service

# Stop all services
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v
```

### Development Setup

```bash
# Start with development overrides (more verbose logging, SQL queries)
docker-compose -f docker-compose.yml -f docker-compose.dev.yml up -d

# View logs
docker-compose logs -f billing-service
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
- **Port**: 5432 (exposed for external tools)
- **Database**: `usagebillingdb`
- **Username**: `usagebilling` (configurable via `DB_USERNAME`)
- **Password**: Configure via `DB_PASSWORD` environment variable
- **Volume**: `postgres-data` (persistent storage)
- **Connection String**: `jdbc:postgresql://postgres:5432/usagebillingdb`

### 3. SonarQube (Code Quality)
- **Port**: 9000
- **URL**: http://localhost:9000
- **Default Login**: admin/admin (change password on first login)
- **Volumes**: 
  - `sonarqube-data` (data)
  - `sonarqube-extensions` (plugins)
  - `sonarqube-logs` (logs)
- **Database**: Separate PostgreSQL instance (`sonarqube-db`)

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

**⚠️ IMPORTANT:** Docker Compose requires environment variables to be set. Create a `.env` file in the project root:

```bash
# Copy the example file
cp .env.example .env

# Edit .env with your secure values
# DO NOT commit .env to version control
```

**Required variables (no defaults - must be set in `.env`):**
- `POSTGRES_PASSWORD` - PostgreSQL database password
- `ADMIN_PASSWORD` - Admin console password
- `API_KEYS` - Comma-separated API keys
- `SONARQUBE_DB_PASSWORD` - SonarQube database password (if using SonarQube)

**Optional variables (have defaults):**
- `POSTGRES_DB` (default: `usagebillingdb`)
- `POSTGRES_USER` (default: `usagebilling`)
- `ADMIN_USERNAME` (default: `admin`)
- `BILLING_SCHEDULER_ENABLED` (default: `false`)

Docker Compose will automatically load `.env` file. See `.env.example` for all available variables.

## Database Access

### Using psql (from host)

```bash
# Connect to PostgreSQL
docker exec -it usage-billing-postgres psql -U usagebilling -d usagebillingdb

# Or using connection string
psql -h localhost -p 5432 -U usagebilling -d usagebillingdb
```

### Using External Tools

- **pgAdmin**: Connect to `localhost:5432`
- **DBeaver**: Use connection string `jdbc:postgresql://localhost:5432/usagebillingdb`
- **IntelliJ Database Tool**: Add PostgreSQL data source

**Note**: H2 console is disabled in Docker (using PostgreSQL). Use PostgreSQL client tools instead.

## Building the Image

```bash
# Build only the application image
docker build -t usage-billing-service:latest .

# Build with specific tag
docker build -t usage-billing-service:1.0.0 .
```

## Running Individual Services

### Start only database

```bash
docker-compose up -d postgres
```

### Start application and database (no SonarQube)

```bash
docker-compose up -d postgres billing-service
```

### Start SonarQube separately

```bash
docker-compose up -d sonarqube sonarqube-db
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

If ports are already in use, modify `docker-compose.yml`:

```yaml
ports:
  - "8081:8080"  # Change host port
```

### Reset everything

```bash
# Stop and remove all containers, networks, and volumes
docker-compose down -v

# Remove images
docker-compose down --rmi all

# Start fresh
docker-compose up -d
```

## Production Considerations

1. **Set secure passwords** in `.env` file (copy from `.env.example`)
2. **Use secrets management** (Docker secrets, Kubernetes secrets, etc.)
3. **Configure resource limits** in `docker-compose.yml`:
   ```yaml
   deploy:
     resources:
       limits:
         cpus: '1'
         memory: 1G
   ```
4. **Enable billing scheduler** for production
5. **Configure proper logging** (file-based, centralized)
6. **Set up monitoring** (Prometheus, Grafana)
7. **Use production-grade PostgreSQL** (managed service recommended)

## SonarQube Setup

1. Start SonarQube:
   ```bash
   docker-compose up -d sonarqube
   ```

2. Wait for SonarQube to be ready (check logs):
   ```bash
   docker-compose logs -f sonarqube
   ```

3. Access http://localhost:9000
4. Login with default credentials (change password immediately)
5. Generate token: Administration → Security → Users → Tokens
6. Run analysis:
   ```bash
   mvn sonar:sonar \
     -Dsonar.host.url=http://localhost:9000 \
     -Dsonar.token=your-token
   ```

