# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM gradle:8.14-jdk21 AS build

WORKDIR /app

# Copy Gradle wrapper and dependency files first (layer cache)
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copy source code and build the fat JAR (skips tests for faster builds)
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /app/build/libs/inventory-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
