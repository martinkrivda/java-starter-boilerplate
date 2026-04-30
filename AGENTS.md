# AGENTS.md

## Project Purpose

This repository is a production-ready Micronaut starter for a future document sealing and signing microservice. At this stage it contains only boilerplate, technical foundations and integration points.

## Architectural Rules

- Respect package boundaries: `api`, `application`, `domain`, `infrastructure`, `config`, `observability`.
- Keep JSON REST responses in the standard `ApiResponse` envelope and keep RFC 9457-style errors centralized in the global handler.
- Keep business orchestration in `application`.
- Keep vendor SDKs and framework-specific code in `infrastructure`.
- Keep `domain` free of Micronaut, JPA and cloud SDK concerns.
- Do not add business logic outside the intended layer.

## Java Runtime

- Target Java version is Oracle JDK 25 LTS.
- Use Gradle Java toolchains with `languageVersion = 25`.
- Do not use Java preview features unless explicitly requested.
- Prefer stable Java 25 language features only.
- Keep code compatible with production JVM execution, not IDE-only behavior.

## Coding Conventions

- Prefer small files and small classes.
- Use readable names over cleverness.
- Prefer composition over inheritance.
- Keep abstractions minimal and justified.
- Avoid dead classes, speculative interfaces and unused extension points.
- Add comments only when they materially improve clarity.
- Follow Micronaut framework conventions for controllers, configuration properties, dependency injection, validation and environment-specific configuration.

## Testing Rules

- Add tests for controllers, services, repository contracts, storage adapter contracts and configuration changes.
- Keep tests deterministic and readable.
- Maintain the Jacoco coverage gate.
- Exclusions are allowed only for bootstrap entry points or generated code and must stay explicitly documented.

## Configuration Rules

- Prefer explicit configuration keys.
- Use profile-based YAML only where it improves clarity.
- Use the `.yaml` extension for YAML files. Do not introduce new `.yml` files.
- Keep PostgreSQL and SQL Server configuration easy to switch through Micronaut environments.
- Use environment variables for deploy-time secrets and infrastructure values.

## REST API Rules

- Public REST API endpoints must use the `/rest/v1` prefix.
- Keep documentation endpoints outside the versioned REST API unless explicitly required.
- `/reference` should remain the documentation UI endpoint.
- `/doc` should return the OpenAPI JSON definition.
- Do not put business logic in controllers.
- Controllers must remain thin and return the standard `ApiResponse<T>` envelope for JSON REST API responses.
- Keep response structure consistent: successful JSON REST responses use `success`, `data`, `error` and `meta`; errors stay centralized through the global RFC 9457-style handler.
- After every REST endpoint change, update the OpenAPI annotations and verify the generated Swagger/OpenAPI document used by `/reference` and `/doc`.
- Keep `/reference`, `/doc`, `/doc/openapi.json` and `/doc/openapi.yaml` aligned with the generated Micronaut OpenAPI document.

## Documentation Rules

- Update `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, or `docs/` whenever behavior, public API, infrastructure, or configuration changes.
- Document new architectural decisions close to the code or in `docs/`.
- Update API documentation whenever REST behavior, routes, request bodies or response shapes change.

## Dependency Management

- Centralize versions in Gradle version catalogs when practical.
- Prefer stable, mainstream libraries.
- Do not add dependencies without a concrete reason tied to the starter scope.

## Scope Guardrails

Do not implement in this phase:

- real PDF sealing
- real PDF signing
- signature validation
- HSM integration
- certificate workflows
- queues
- orchestration-heavy process managers

## Vibe Coding Principles

- Optimize for readability first.
- Keep onboarding friction low.
- Minimize framework magic.
- Prefer pragmatic solutions over enterprise overengineering.
- Document important tradeoffs and decisions.
- Use Spotless as the formatting authority and keep `.spotlessignore` aligned with any exclusions.
- Recommend IntelliJ IDEA when tooling guidance is needed.
- Treat `gradle.properties` `projectVersion` as the canonical version source and update `CHANGELOG.md` with every version change.
