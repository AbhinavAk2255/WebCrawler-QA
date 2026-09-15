# Stage 1: Build the Java Spring Boot Application
FROM maven:3.9.16-eclipse-temurin-25 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Runtime Container running Ubuntu 22.04 (Matches your local machine)
FROM ubuntu:22.04
WORKDIR /app

# Install Java 25 and the precise system libraries needed for Playwright
RUN apt-get update && apt-get install -y \
    openjdk-21-jre-headless \
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

# Copy the compiled executable JAR file from Stage 1
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

# Starts your service matching Render's assigned port variable
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]
