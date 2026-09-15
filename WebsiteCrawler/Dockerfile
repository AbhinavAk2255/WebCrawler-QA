# Stage 1: Build the Spring Boot Application
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests


# Stage 2: Balanced Runtime Environment
FROM eclipse-temurin:25-jre
WORKDIR /app

# Switch to root user to perform package installations
USER root

# Install the correct, modern system libraries required by Playwright browsers
RUN apt-get update && apt-get install -y \
    wget \
    gnupg \
    libatk1.0-0 \
    libatk-bridge2.0-0 \
    libgdk-pixbuf2.0-0 \
    libgtk-3-0 \
    libgbm1 \
    libnss3 \
    libxss1 \
    libasound2 \
    && rm -rf /var/lib/apt/lists/*

# Copy the compiled JAR from Stage 1
COPY --from=build /app/target/*.jar app.jar

# Define Playwright environment variables to force internal browser downloads during execution
ENV PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD=0

EXPOSE 8080

# Starts the application dynamically matching Render's PORT configuration
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
