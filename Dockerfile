# syntax=docker/dockerfile:1

# --- Build stage: Maven ---
FROM maven:3.9-eclipse-temurin-21 AS build
# The pom is copied here and resolves sources as ${project.basedir}/../src/...,
# i.e. /app/src/... — so the build WORKDIR must be /app/backend.
WORKDIR /app/backend

# Cache resolution. Copy only the Maven file first.
COPY backend/pom.xml ./pom.xml
RUN mvn -q -f pom.xml dependency:go-offline

# Application sources live outside the backend module directory.
COPY src/main/java ../src/main/java
COPY src/main/resources ../src/main/resources

# Tests are skipped in the image build; the final test run runs locally.
RUN mvn -q -f pom.xml -DskipTests package

# --- Runtime stage ---
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod \
    JAVA_OPTS="-Xms128m -Xmx256m"
EXPOSE 8080

COPY --from=build /app/backend/target/*.jar /app/app.jar

# OPENCODE_DATA_DIR is bind-mounted read-only at runtime (see docker-compose.yml).
VOLUME ["/data/opencode"]

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar \"$@\""]
