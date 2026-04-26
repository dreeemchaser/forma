# ── Stage 1: Build ────────────────────────────────────────────────────────────
FROM eclipse-temurin:26-jdk AS build

WORKDIR /app

# Copy Gradle wrapper and dependency manifests first (better layer caching)
COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle

# Pre-fetch dependencies (cached unless build.gradle changes)
RUN ./gradlew dependencies --no-daemon -q || true

# Copy source and build the fat JAR, skipping tests (tests use H2, not needed at build time)
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

# ── Stage 2: Runtime ──────────────────────────────────────────────────────────
FROM eclipse-temurin:26-jdk AS runtime

WORKDIR /app

# Create a non-root user for security
RUN addgroup --system forma && adduser --system --ingroup forma forma

# Copy only the built JAR from the build stage
COPY --from=build /app/build/libs/*.jar app.jar

RUN chown forma:forma app.jar

USER forma

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
