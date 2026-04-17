# java-starter-boilerplate

Production-grade Micronaut starter for a future web microservice focused on document sealing and signing. This stage provides only the technical baseline: architecture, persistence, storage, PDF/DSS integration points, observability, Docker, Kubernetes and test infrastructure.

## Architecture

The project uses a pragmatic layered layout:

- `api`
- `application`
- `domain`
- `infrastructure.persistence`
- `infrastructure.storage`
- `infrastructure.document`
- `config`
- `observability`

More detail is in [docs/architecture.md](docs/architecture.md).

## Project Tree

```text
.
├── .github/workflows/ci.yaml
├── AGENTS.md
├── CHANGELOG.md
├── CONTRIBUTING.md
├── Dockerfile
├── LICENSE.md
├── Makefile
├── README.md
├── build.gradle.kts
├── docker-compose.yaml
├── docs
│   ├── api-response.md
│   ├── api-response.schema.json
│   ├── architecture.md
│   ├── configuration.md
│   ├── deployment.md
│   └── monitoring.md
├── gradle
│   └── libs.versions.toml
├── gradlew
├── gradlew.bat
├── k8s
│   ├── configmap.yaml
│   ├── deployment.yaml
│   ├── secret.yaml
│   └── service.yaml
└── src
    ├── main
    │   ├── java/com/example/javastarterboilerplate
    │   │   ├── api
    │   │   ├── application
    │   │   ├── config
    │   │   ├── domain
    │   │   ├── infrastructure
    │   │   └── observability
    │   └── resources
    │       ├── application*.yaml
    │       ├── db/migration
    │       └── logback.xml
    └── test
        └── java/com/example/javastarterboilerplate
```

## Local Run

Requirements:

- Java SE Development Kit 25.0.2
- Docker optional

Recommended IDE:

- IntelliJ IDEA is the default recommendation for Java, Micronaut and Gradle work in this repository.

Formatter:

- Spotless is the repository formatter.
- Java sources use the Eclipse JDT formatter through Spotless.
- The formatter ignore file is `.spotlessignore`.

Common commands:

```bash
make help
make doctor
make run
make check
make format
make jar
```

## API Response Standard

Application JSON endpoints use a standard response envelope:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2026-02-04T10:30:00Z"
  }
}
```

Error responses use the same envelope and embed RFC 9457-style problem details with company extensions:

- `error.type`
- `error.title`
- `error.status`
- `error.instance`
- `error.code`
- `error.requestId`
- `error.errors[]` for validation issues

Canonical request correlation header:

- `X-Request-Id`

Compatibility note:

- The service also echoes `X-Correlation-Id` with the same value during the transition to the new standard.
- Raw OpenAPI artifacts at `/doc/openapi.json` and `/doc/openapi.yaml` stay outside the envelope because they must remain valid OpenAPI documents.

Reference material:

- [docs/api-response.md](docs/api-response.md)
- [docs/api-response.schema.json](docs/api-response.schema.json)

Default JSON API route prefix is `/rest/v1`, configured through `app.api.prefix` and overridable with `APP_API_PREFIX`.

API documentation routes:

- `/doc` renders the Scalar-based API reference UI.
- `/doc/openapi.json` returns the generated OpenAPI document as JSON.
- `/doc/openapi.yaml` returns the generated OpenAPI document as YAML.
- `/doc/openapi.yaml/download` forces YAML download.
- `/reference` is kept as a compatibility alias for `/doc`.

Monitoring route:

- `/metrics` exposes Prometheus metrics in text format (`text/plain; version=0.0.4`).

Operational shutdown behavior:

- `/health/ready` flips to `not_ready` during application draining.
- container images and `docker-compose` include an HTTP health check based on readiness.

## Development Without Local Java

If your PC does not have Java installed, you can still develop against this repository with Docker only.

Minimal host requirements:

- Docker Desktop or Docker Engine
- a code editor

Recommended workflow:

1. Run checks in Docker:

```bash
make docker-check
```

2. Apply formatting in Docker:

```bash
make docker-format
```

3. Build the executable JAR in Docker:

```bash
make docker-jar
```

This workflow uses an `eclipse-temurin:25-jdk` container, mounts the repository into `/workspace`, and runs Gradle inside the container. It is the recommended path when the host machine does not have Java SE Development Kit 25.0.2 installed.

If you want to run the application container itself instead of Gradle tasks, use:

```bash
docker compose up -d app
```

Cleanup commands after Docker-only work:

```bash
make clean-deep
make docker-clean
make reset-workspace
```

What these remove:

- `make clean-deep`: local `build/`, `logs/`, `.gradle/` and `.gradle-wrapper/`
- `make docker-clean`: Compose containers, networks, named volumes, local app image and pulled helper images used by this project
- `make reset-workspace`: both groups above

The `apt-get` packages installed inside temporary Gradle containers do not persist on the host. What remains after Docker-based development is primarily:

- generated local files in the repository
- Docker images
- Docker volumes
- Docker build cache

Run with the default zero-dependency profile:

```bash
./gradlew run
```

The default startup path does not require PostgreSQL, SQL Server, Redis, MinIO or any other external service. The sample-document API uses an in-memory repository until a persistence profile is explicitly enabled.

Run with PostgreSQL:

```bash
MICRONAUT_ENVIRONMENTS=dev,postgresql ./gradlew run
```

Run with SQL Server:

```bash
MICRONAUT_ENVIRONMENTS=dev,sqlserver ./gradlew run
```

Run local dependencies with Docker Compose when you want to exercise infrastructure-backed profiles:

```bash
docker compose up -d postgres minio
MICRONAUT_ENVIRONMENTS=prod,postgresql docker compose up -d app
```

Run the application container without external dependencies:

```bash
docker compose up -d app
```

## Build And Test

```bash
./gradlew clean check
```

Coverage reports are generated in `build/reports/jacoco/test/html/index.html`. The build is configured to fail when line coverage for production code drops below 100%, excluding only the bootstrap `Application` entry point.

## Build Executable JAR

Build the executable fat JAR:

```bash
./gradlew clean fatJar
```

Without local Java:

```bash
make docker-jar
```

Artifact:

```text
build/libs/java-starter-boilerplate-<version>-all.jar
```

Run the service from the JAR:

```bash
java -jar build/libs/java-starter-boilerplate-<version>-all.jar serve
```

Run CLI-only commands:

```bash
java -jar build/libs/java-starter-boilerplate-<version>-all.jar help
java -jar build/libs/java-starter-boilerplate-<version>-all.jar version
java -jar build/libs/java-starter-boilerplate-<version>-all.jar env
```

Running without arguments also starts the HTTP service. The CLI is intentionally lightweight and limited to bootstrap or diagnostic commands; future business workflows should stay in application services, not in ad-hoc terminal scripts.

## API Documentation

OpenAPI documentation is generated at compile time with Micronaut OpenAPI and served by the application itself.

Routes:

- `GET /doc`
- `GET /doc/openapi.json`
- `GET /doc/openapi.json/download`
- `GET /doc/openapi.yaml`
- `GET /doc/openapi.yaml/download`
- `GET /metrics`

The runtime endpoints normalize the exposed document to OpenAPI `3.1.1` and provide a Scalar reference UI backed by the generated JSON definition.
Prometheus-compatible runtime metrics are exposed at `/metrics`.

## Persistence

- Micronaut Data + Hibernate JPA
- Flyway migrations
- Hibernate auto-DDL disabled so Flyway remains the single schema authority
- PostgreSQL profile in `application-postgresql.yaml`
- SQL Server profile in `application-sqlserver.yaml`
- H2 default and test profiles for onboarding and CI speed

The sample persistence baseline contains:

- `SampleDocument` domain model
- JPA entity and Micronaut Data repository
- persistence adapter
- vendor-specific seed migrations

## S3 And MinIO

The storage layer is exposed through the `domain.storage.ObjectStorage` contract and implemented by `infrastructure.storage.S3ObjectStorageAdapter`.

The adapter is prepared for:

- original PDF storage
- signed document storage
- temporary artifact storage

Configuration is documented in [docs/configuration.md](docs/configuration.md) and exemplified in `.env.example`.

## PDFBox And DSS

Current state:

- PDFBox is integrated for PDF inspection metadata.
- DSS is integrated for placeholder document preparation.
- No sealing, signing or validation workflow is implemented yet.

Future business logic belongs in:

- `application` for orchestration
- `infrastructure.document` for concrete library interactions

## Logging

Logging uses Logback with:

- readable console logs
- optional JSON file logs
- weekly rotation
- retention via `LOG_MAX_HISTORY`
- gzip compression for archived files
- separate application and error files when file logging is enabled

Default container behavior keeps file logging off and relies on stdout. Bare-metal or VM deployments can enable file logging with `FILE_LOGGING_ENABLED=true`.

## Docker

The repository includes a multi-stage `Dockerfile` with:

- build stage on JDK 25
- runtime stage on JRE 25
- non-root runtime user

Local Compose support is provided in `docker-compose.yaml` for:

- PostgreSQL
- MinIO
- optional application container profile

## Kubernetes

The `k8s/` directory contains:

- `configmap.yaml`
- `secret.yaml`
- `service.yaml`
- `deployment.yaml`

These include:

- resource requests/limits
- readiness probe
- liveness probe
- config and secret placeholders

## Coding Conventions

- Keep code explicit and readable.
- Prefer composition over inheritance.
- Avoid speculative abstractions.
- Keep files and classes small.
- Add comments only where they materially improve clarity.
- Update `README.md`, `CONTRIBUTING.md`, or `docs/` whenever behavior, public API, infrastructure, or configuration changes.

## Versioning And Changelog

Write the canonical application version in `gradle.properties` under `projectVersion`.

When the version changes:

- update `projectVersion`
- add an entry to `CHANGELOG.md`
- update docs if the release changes public behavior or operations

## What Is Placeholder

- real PDF sealing
- real PDF signing
- signature validation
- HSM integration
- certificate lifecycle workflows
- queueing and orchestration layers

These intentionally remain out of scope in this phase.
