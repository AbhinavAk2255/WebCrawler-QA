# Use a unified image that natively comes with Eclipse Temurin Java 25 and Ubuntu under the hood
FROM maven:3.9.16-eclipse-temurin-25

WORKDIR /app

# Switch to root user to handle system package installations safely
USER root

# Install the correct system libraries that Playwright browsers need to run on this image
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
    && rm -rf /var/lib/apt/lists/*

# Copy your source configuration and directories into the container workspace
COPY pom.xml .
COPY src ./src

# Compile your application directly inside the Java 25 environment
RUN mvn clean package -DskipTests

EXPOSE 8080

# Run the compiled jar file using the exact same Java 25 runtime environment context
CMD ["sh", "-c", "java -jar target/*.jar --server.port=${PORT:-8080}"]
