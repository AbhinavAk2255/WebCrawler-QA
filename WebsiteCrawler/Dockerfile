# Stage 1: Build the Spring Boot Application using Java 25
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Balanced Runtime Environment using the EXACT SAME Java 25 base
FROM eclipse-temurin:25-jre
WORKDIR /app

# Switch to root user to perform setup tasks safely
USER root

# Install the correct system libraries that Playwright browsers need to run natively
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
    libasound2t64 \
    && rm -rf /var/lib/apt/lists/*

# Copy the compiled executable JAR file straight from Stage 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Starts your service instantly, matching Render's assigned port variable
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
