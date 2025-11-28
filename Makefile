.PHONY: help build build-dev up up-dev down down-dev start start-dev stop stop-dev restart restart-dev reset reset-dev logs logs-dev ps ps-dev clean sonar sonar-dev

# Default target
help:
	@echo "Usage: make [target]"
	@echo ""
	@echo "Development targets:"
	@echo "  build-dev    - Build development Docker images"
	@echo "  start-dev    - Start development services (alias for up-dev)"
	@echo "  up-dev       - Start development services"
	@echo "  stop-dev     - Stop development services (alias for down-dev)"
	@echo "  down-dev     - Stop development services"
	@echo "  restart-dev  - Restart development services"
	@echo "  reset-dev    - Stop, remove volumes, and restart development services"
	@echo "  logs-dev     - Show development service logs"
	@echo "  ps-dev       - Show development service status"
	@echo ""
	@echo "Production targets:"
	@echo "  build        - Build production Docker images"
	@echo "  start        - Start production services (alias for up)"
	@echo "  up           - Start production services"
	@echo "  stop         - Stop production services (alias for down)"
	@echo "  down         - Stop production services"
	@echo "  restart      - Restart production services"
	@echo "  reset        - Stop, remove volumes, and restart production services"
	@echo "  logs         - Show production service logs"
	@echo "  ps           - Show production service status"
	@echo ""
	@echo "Utility targets:"
	@echo "  clean        - Remove all containers, volumes, and images"
	@echo ""
	@echo "SonarQube targets:"
	@echo "  sonar        - Run SonarQube analysis (production)"
	@echo "  sonar-dev    - Run SonarQube analysis (development)"

# Development targets (uses local.env via env_file in docker-compose.dev.yml)
build-dev:
	docker-compose -f docker-compose.dev.yml --env-file local.env build

start-dev: up-dev

up-dev:
	docker-compose -f docker-compose.dev.yml --env-file local.env up -d

stop-dev: down-dev

down-dev:
	docker-compose -f docker-compose.dev.yml down

restart-dev:
	docker-compose -f docker-compose.dev.yml restart

reset-dev:
	docker-compose -f docker-compose.dev.yml down -v
	docker-compose -f docker-compose.dev.yml --env-file local.env up -d

logs-dev:
	docker-compose -f docker-compose.dev.yml logs -f

ps-dev:
	docker-compose -f docker-compose.dev.yml ps

# Production targets (uses prod.env via env_file in docker-compose.yml)
build:
	docker-compose --env-file prod.env build

start: up

up:
	docker-compose --env-file prod.env up -d

stop: down

down:
	docker-compose --env-file prod.env down

restart:
	docker-compose --env-file prod.env restart

reset:
	docker-compose --env-file prod.env down -v
	docker-compose --env-file prod.env up -d

logs:
	docker-compose logs -f

ps:
	docker-compose ps

# Utility targets
clean:
	docker-compose -f docker-compose.dev.yml down -v
	docker-compose down -v
	docker system prune -f

# SonarQube analysis targets
# With SONAR_FORCEAUTHENTICATION=false, no token is needed - works automatically
sonar:
	mvn clean test sonar:sonar

sonar-dev:
	mvn clean test sonar:sonar -Dsonar.host.url=http://localhost:9000

