# Stage 1: Build the Spring Boot Application
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Environment
FROM eclipse-temurin:25-jre
WORKDIR /app

# Switch to root user to install packages
USER root

# Install minimal core tools needed for setup scripts
RUN apt-get update && apt-get install -y \
    wget \
    curl \
    gnupg \
    && rm -rf /var/lib/apt/lists/*

# Copy your compiled JAR file from Stage 1
COPY --from=build /app/target/*.jar app.jar

# --- THE MAGIC FIX FOR PLAYWRIGHT DEPENDENCIES ---
# We download the Playwright CLI tool and tell it to figure out the OS package names automatically
RUN curl -fsSL https://nodesource.com | bash - \
    && apt-get install -y nodejs \
    && npx playwright install-deps chromium \
    && apt-get purge -y nodejs curl \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

# Starts the application dynamically matching Render's PORT configuration
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
