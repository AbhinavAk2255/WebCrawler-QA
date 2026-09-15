# Stage 1: Build the Spring Boot Application using Java 25
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Container using the EXACT SAME Java 25 base
FROM eclipse-temurin:25-jre
WORKDIR /app

USER root

# Install nodejs, npm, and curl so Playwright's native installer script can run
RUN apt-get update && apt-get install -y curl nodejs npm && rm -rf /var/lib/apt/lists/*

# Copy the compiled executable JAR file straight from Stage 1
COPY --from=build /app/target/*.jar app.jar

# --- AUTOMATED PLAYWRIGHT DEPENDENCY INSTALLER ---
# This forces Playwright to auto-detect the correct OS library names dynamically on Render
RUN npx playwright install-deps chromium \
    && apt-get purge -y nodejs npm curl \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

# Starts your service instantly, matching Render's assigned port variable
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
