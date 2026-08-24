FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

ARG BUILD_NUMBER
ARG GIT_COMMIT
ARG BUILD_TIMESTAMP

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle

RUN chmod +x gradlew

COPY src src

RUN set -eu; \
    if [ -n "${BUILD_NUMBER:-}" ]; then export BUILD_NUMBER; fi; \
    if [ -n "${GIT_COMMIT:-}" ]; then export GIT_COMMIT; fi; \
    if [ -n "${BUILD_TIMESTAMP:-}" ]; then export BUILD_TIMESTAMP; fi; \
    ./gradlew --no-daemon clean installDist

FROM eclipse-temurin:25-jre

ARG BUILD_NUMBER
ARG GIT_COMMIT
ARG BUILD_TIMESTAMP
ARG APP_VERSION=0.1.0-SNAPSHOT

LABEL org.opencontainers.image.title="java-starter-boilerplate" \
      org.opencontainers.image.version="${APP_VERSION}" \
      org.opencontainers.image.revision="${GIT_COMMIT}" \
      org.opencontainers.image.created="${BUILD_TIMESTAMP}" \
      org.opencontainers.image.source="java-starter-boilerplate" \
      org.opencontainers.image.build.number="${BUILD_NUMBER}"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --uid 10001 appuser

WORKDIR /app

COPY --from=build /workspace/build/install/java-starter-boilerplate /app

ENV MICRONAUT_ENVIRONMENTS=prod
ENV APP_NAME=java-starter-boilerplate
ENV APP_VERSION=${APP_VERSION}

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=5 \
    CMD sh -c 'scheme=http; [ "$TLS_ENABLED" = "true" ] && scheme=https; \
    curl -fsSk ${scheme}://127.0.0.1:8080/readyz | grep -q "\"status\":\"ready\"" || exit 1'

ENTRYPOINT ["./bin/java-starter-boilerplate"]
