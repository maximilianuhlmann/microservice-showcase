.PHONY: help build build-prod up up-prod down down-prod start start-prod stop stop-prod restart restart-prod reset reset-prod logs logs-prod ps ps-prod clean sonar sonar-dev rebuild rebuild-prod dev

# Compose file variables
COMPOSE_DEV = -f docker-compose.base.yml -f docker-compose.dev.yml
COMPOSE_PROD = -f docker-compose.base.yml -f docker-compose.prod.yml
ENV_DEV = --env-file local.env
ENV_PROD = --env-file prod.env

# Default target
help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Default targets (development):"
	@echo "  build        - Build Docker images"
	@echo "  start        - Start services (alias for up)"
	@echo "  up           - Start services"
	@echo "  stop         - Stop services (alias for down)"
	@echo "  down         - Stop services"
	@echo "  restart      - Restart services"
	@echo "  rebuild      - Rebuild and restart services (after code changes)"
	@echo "  reset        - Stop, remove volumes, and restart services"
	@echo "  logs         - Show service logs"
	@echo "  ps           - Show service status"
	@echo ""
	@echo "Production targets:"
	@echo "  build-prod   - Build production Docker images"
	@echo "  start-prod   - Start production services"
	@echo "  stop-prod    - Stop production services"
	@echo "  restart-prod - Restart production services"
	@echo "  rebuild-prod - Rebuild and restart production services"
	@echo "  reset-prod   - Stop, remove volumes, and restart production services"
	@echo "  logs-prod    - Show production service logs"
	@echo "  ps-prod      - Show production service status"
	@echo ""
	@echo "Utility targets:"
	@echo "  clean        - Remove all containers, volumes, and images"
	@echo ""
	@echo "SonarQube targets:"
	@echo "  sonar        - Run SonarQube analysis (production)"
	@echo "  sonar-dev    - Run SonarQube analysis (development)"
	@echo ""
	@echo "Convenience aliases:"
	@echo "  dev          - Start development services (alias for start-dev)"

# Convenience aliases
dev: start

# Default targets (development)
build:
	docker-compose $(COMPOSE_DEV) $(ENV_DEV) build

start: up

up:
	docker-compose $(COMPOSE_DEV) $(ENV_DEV) up -d

stop: down

down:
	docker-compose $(COMPOSE_DEV) down

restart:
	docker-compose $(COMPOSE_DEV) $(ENV_DEV) restart

rebuild: build restart

reset:
	docker-compose $(COMPOSE_DEV) down -v
	docker-compose $(COMPOSE_DEV) $(ENV_DEV) up -d

logs:
	docker-compose $(COMPOSE_DEV) logs -f

ps:
	docker-compose $(COMPOSE_DEV) ps

# Production targets
build-prod:
	docker-compose $(COMPOSE_PROD) $(ENV_PROD) build

start-prod: up-prod

up-prod:
	docker-compose $(COMPOSE_PROD) $(ENV_PROD) up -d

stop-prod: down-prod

down-prod:
	docker-compose $(COMPOSE_PROD) down

restart-prod:
	docker-compose $(COMPOSE_PROD) $(ENV_PROD) restart

rebuild-prod: build-prod restart-prod

reset-prod:
	docker-compose $(COMPOSE_PROD) $(ENV_PROD) down -v
	docker-compose $(COMPOSE_PROD) $(ENV_PROD) up -d

logs-prod:
	docker-compose $(COMPOSE_PROD) logs -f

ps-prod:
	docker-compose $(COMPOSE_PROD) ps

# Utility targets
clean:
	docker-compose $(COMPOSE_DEV) down -v
	docker-compose $(COMPOSE_PROD) down -v
	docker system prune -f

# SonarQube analysis targets
# With SONAR_FORCEAUTHENTICATION=false, no token is needed - works automatically
sonar:
	mvn clean test sonar:sonar

sonar-dev:
	mvn clean test sonar:sonar -Dsonar.host.url=http://localhost:9000

