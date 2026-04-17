# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Production-ready Micronaut (not Spring Boot) starter for document sealing/signing services. Built with Java 25, Gradle (Kotlin DSL), and layered architecture with strict boundary enforcement.

## Common Commands

```bash
# Run locally (requires Java 25.0.2)
make run                    # Start with H2 (default profile)
make run-postgresql         # Start with PostgreSQL
make run-sqlserver          # Start with SQL Server

# Quality checks (run before committing)
make check                  # Tests + 100% coverage gate + Spotless check
make format                 # Apply Spotless formatting
make test                   # Tests only

# Build
make jar                    # Build executable fat JAR

# Docker-based workflows (no local Java required)
make docker-check           # Full quality check in Docker
make docker-format          # Format in Docker
make docker-jar             # Build JAR in Docker

# Cleanup
make clean-deep             # Clean build artifacts and Gradle state
make docker-clean           # Remove Docker containers/volumes/images
```

### Gradle equivalents

```bash
./gradlew clean check           # Full build + tests + coverage + format check
./gradlew test                  # Tests only
./gradlew spotlessApply         # Apply formatting
./gradlew clean fatJar          # Build fat JAR
./gradlew run                   # Run with default profile
```

### Running a single test

```bash
./gradlew test --tests "com.example.javastarterboilerplate.SomeTest"
./gradlew test --tests "com.example.javastarterboilerplate.SomeTest.methodName"
```

## Architecture

### Package Layers (strict boundaries)

```
com.example.javastarterboilerplate/
├── api/           # HTTP only — controllers, DTOs, ApiResponse<T> envelope, GlobalErrorHandler
├── application/   # Business orchestration — service classes, no framework annotations beyond @Singleton
├── domain/        # Framework-free — interfaces/contracts (Repository, ObjectStorage), records as models
├── infrastructure/# Vendor adapters — JPA entities, Micronaut Data repos, S3, PDFBox, DSS, Flyway
├── config/        # Cross-cutting Micronaut @Factory / @ConfigurationProperties beans
└── observability/ # RequestIdFilter (X-Request-Id header + MDC), metrics
```

**Layer rules:**
- `domain` has zero framework imports
- `application` depends on `domain` only
- `api` depends on `application` and `domain`
- `infrastructure` implements `domain` contracts — nothing else depends on it directly

### Standard Response Envelope

All JSON endpoints return `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": { "requestId": "...", "timestamp": "..." }
}
```

Errors follow RFC 9457 via `ApiProblemDetails`. Health (`/health/**`) and metrics (`/metrics`) are **not** wrapped.

### Database & Migrations

- Flyway is the single source of truth for schema — Hibernate DDL auto is disabled.
- Migrations: `src/main/resources/db/migration/common/` for portable SQL, vendor-specific under `h2/`, `postgresql/`, `sqlserver/`.
- Profiles: `h2` (default/test), `postgresql`, `sqlserver`.

### Key Infrastructure Adapters

| Contract (domain) | Adapter (infrastructure) |
|---|---|
| `ObjectStorage` | `S3ObjectStorageAdapter` (S3/MinIO) |
| `PdfDocumentService` | `PdfBoxDocumentService` |
| `DigitalSignatureService` | `DssDigitalSignatureService` |
| `SampleDocumentRepository` | `SampleDocumentPersistenceAdapter` → `SampleDocumentJpaRepository` |

## Testing

- **Framework:** JUnit 5 + Micronaut Test (`@MicronautTest`) + Mockito + AssertJ.
- **Coverage gate:** 100% line coverage on all production code except `Application.java` bootstrap. The `./gradlew check` task enforces this — the build fails if coverage drops below 100%.
- **Test profile:** `application-test.yaml` uses H2 in-memory DB automatically.
- Coverage reports: `build/reports/jacoco/test/html/index.html`

## Code Conventions

- Prefer **records** for domain models and DTOs.
- Keep classes small and focused; prefer **composition over inheritance**.
- No premature abstractions — add only what the current task requires.
- Conventional Commits: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`
- Branch prefixes: `feature/`, `bugfix/`, `hotfix/`, `release/`

## Code Formatting

Spotless with Eclipse JDT handles Java formatting (`config/spotless/eclipse-java-formatter.xml`). Always run `make format` (or `./gradlew spotlessApply`) before committing. The CI pipeline runs `spotlessCheck` and will fail on unformatted code.

## Configuration & Profiles

- Environment variables are defined with defaults in `application.yaml`; see `.env.example` for the full list.
- Profile-specific overrides: `application-{profile}.yaml`.
- `APP_VERSION` is injected from `gradle.properties` at build time via `@projectVersion@` token substitution.
- S3/MinIO and DSS document services are toggled via `STORAGE_S3_ENABLED`, `PDFBOX_ENABLED`, `DSS_ENABLED`.

## API & Docs

- Scalar UI: `GET /doc`
- OpenAPI JSON: `GET /doc/openapi.json`
- OpenAPI YAML: `GET /doc/openapi.yaml`
- Generated at compile time by Micronaut OpenAPI.

## Scope Guardrails

This phase intentionally excludes real PDF sealing, cryptographic signing, and HSM integration. Focus is on technical infrastructure and architectural patterns.
