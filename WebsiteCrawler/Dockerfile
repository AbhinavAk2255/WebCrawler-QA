
# Stage 1: Build Spring Boot app

FROM maven:3.9.16-eclipse-temurin-25-noble AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests



# Stage 2: Run Spring Boot + Playwright


FROM eclipse-temurin:25-jre-noble

WORKDIR /app

USER root

# Install Node.js and npm temporarily.
# We use the SAME Playwright version as the Java dependency.
RUN apt-get update \
    && apt-get install -y nodejs npm \
    && rm -rf /var/lib/apt/lists/*

# Store Playwright browsers in a fixed shared location.
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# Install the EXACT Playwright version used by the Java application.
# Java pom.xml currently uses Playwright 1.52.0.
RUN npx -y playwright@1.52.0 install --with-deps chromium

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Remove Node.js/npm because they are no longer needed at runtime.
RUN apt-get purge -y nodejs npm \
    && apt-get autoremove -y \
    && rm -rf /var/lib/apt/lists/*

EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]