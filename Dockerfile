FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl unzip \
    && rm -rf /var/lib/apt/lists/*

COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts gradle.properties ./
COPY gradle gradle

RUN chmod +x gradlew

COPY src src

RUN ./gradlew --no-daemon clean installDist

FROM eclipse-temurin:25-jre

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --create-home --uid 10001 appuser

WORKDIR /app

COPY --from=build /workspace/build/install/java-starter-boilerplate /app

ENV MICRONAUT_ENVIRONMENTS=prod
ENV FILE_LOGGING_ENABLED=false
ENV APP_NAME=java-starter-boilerplate

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=5 \
    CMD curl -fsS http://127.0.0.1:8080/health/ready | grep -q '"status":"ready"' || exit 1

ENTRYPOINT ["./bin/java-starter-boilerplate"]
