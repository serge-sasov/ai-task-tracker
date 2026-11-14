up:
	docker compose -f docker-compose.yml up -d --build

down:
	docker compose -f docker-compose.yml down

test:
	mvn -f backend/pom.xml test

build:
	mvn -f backend/pom.xml package

logs:
	docker compose -f docker-compose.yml logs -f

.PHONY: up down test build logs
