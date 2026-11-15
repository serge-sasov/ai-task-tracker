up:
	docker compose -f docker-compose.yml up -d --build

down:
	docker compose -f docker-compose.yml down

test:
	mvn -f backend/pom.xml clean test

build:
	mvn -f backend/pom.xml clean package
	@echo "Backend jar:"
	@ls -1 backend/target/*.jar

logs:
	docker compose -f docker-compose.yml logs -f

.PHONY: up down test build logs
