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
FROM node:24.19.0-bookworm-slim AS web
WORKDIR /web
COPY apps/web/package*.json ./
RUN --mount=type=secret,id=node-ca \
    if [ -f /run/secrets/node-ca ]; then export NODE_EXTRA_CA_CERTS=/run/secrets/node-ca; fi; \
    npm ci --fetch-retries=1 --fetch-timeout=60000
COPY apps/web/ ./
RUN npm run build

FROM caddy:2.10.2 AS proxy
FROM eclipse-temurin:21-jre
RUN useradd -u 10001 -m safe && mkdir -p /data /site /config /app && chown -R safe:safe /data /config /app
COPY --from=proxy /usr/bin/caddy /usr/bin/caddy
COPY --from=build /build/target/safe-api-0.1.0.jar /app/api.jar
COPY --from=web /web/dist /site
COPY --chmod=644 infra/deployment/hosted/Caddyfile /app/Caddyfile
COPY --chmod=755 infra/deployment/hosted/start.sh /app/start.sh
USER safe
WORKDIR /app
ENV PORT=10000
EXPOSE 10000
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 CMD curl --fail --silent "http://localhost:${PORT}/actuator/health" || exit 1
ENTRYPOINT ["/app/start.sh"]
