# Stage 1: Build Spring Boot app
FROM maven:3.9.16-eclipse-temurin-25-noble AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -DskipTests clean package

# Stage 2: Runtime
FROM eclipse-temurin:25-jre-noble
WORKDIR /app
USER root

ENV DEBIAN_FRONTEND=noninteractive
ENV PLAYWRIGHT_BROWSERS_PATH=/ms-playwright

# (Optional but recommended) limit Java heap on small instances
ENV JAVA_TOOL_OPTIONS="-Xms128m -Xmx384m"

# Install Node + npm (needed to install Playwright browsers during image build)
RUN apt-get update \
  && apt-get install -y --no-install-recommends nodejs npm \
  && rm -rf /var/lib/apt/lists/*

# IMPORTANT:
# Install Playwright browsers + OS deps at BUILD time.
# Install ALL browsers to prevent runtime downloading Firefox/WebKit.
RUN npx -y playwright@1.52.0 install --with-deps

# Copy Spring Boot JAR
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java -jar app.jar --server.port=${PORT:-8080}"]