# syntax=docker/dockerfile:1
FROM maven:3.9.11-eclipse-temurin-21 AS build
WORKDIR /build
COPY apps/api/pom.xml ./pom.xml
# Optional cloud proxy/trust bindings; ordinary local builds use Maven defaults.
RUN --mount=type=secret,id=maven-settings --mount=type=secret,id=java-cacerts \
    set -eu; \
    settings=""; \
    if [ -f /run/secrets/maven-settings ]; then settings="-s /run/secrets/maven-settings -Dmaven.repo.local=/root/.m2/repository"; fi; \
    if [ -f /run/secrets/java-cacerts ]; then export MAVEN_OPTS="-Djavax.net.ssl.trustStore=/run/secrets/java-cacerts"; fi; \
    mvn -B -ntp $settings dependency:go-offline
COPY apps/api/src ./src
RUN --mount=type=secret,id=maven-settings --mount=type=secret,id=java-cacerts \
    set -eu; \
    settings=""; \
    if [ -f /run/secrets/maven-settings ]; then settings="-s /run/secrets/maven-settings -Dmaven.repo.local=/root/.m2/repository"; fi; \
    if [ -f /run/secrets/java-cacerts ]; then export MAVEN_OPTS="-Djavax.net.ssl.trustStore=/run/secrets/java-cacerts"; fi; \
    mvn -B -ntp $settings package -DskipTests
FROM eclipse-temurin:21-jre
RUN useradd -u 10001 -m safe && mkdir /data && chown safe:safe /data
USER safe
WORKDIR /data
COPY --from=build /build/target/safe-api-0.1.0.jar /app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 CMD curl --fail --silent "http://localhost:${PORT:-8080}/actuator/health" || exit 1
ENTRYPOINT ["java","-jar","/app.jar"]
