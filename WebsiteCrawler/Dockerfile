# Stage 1: Build Spring Boot app
FROM maven:3.9.16-eclipse-temurin-25-noble AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -DskipTests clean package


# Stage 2: Run Spring Boot + Playwright
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
USER root

ENV DEBIAN_FRONTEND=noninteractive

# Store Playwright browsers in a fixed shared location (important)
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# Install Node.js/npm temporarily for installing Playwright browsers
RUN apt-get update \
  && apt-get install -y --no-install-recommends nodejs npm \
  && rm -rf /var/lib/apt/lists/*

# Install ONLY Chromium + required OS deps (no Firefox/WebKit)
RUN npx -y playwright@1.52.0 install --with-deps chromium

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

# Remove Node.js/npm (optional) BUT DO NOT autoremove (it may remove Playwright deps)
RUN apt-get purge -y nodejs npm \
  && rm -rf /var/lib/apt/lists/* /root/.npm /tmp/*

EXPOSE 8080

CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]