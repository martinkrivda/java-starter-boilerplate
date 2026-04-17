# Changelog

All notable changes to this project should be documented in this file.

The canonical application version is stored in `gradle.properties` under `projectVersion`.
When releasing or preparing a new version:

1. Update `projectVersion` in `gradle.properties`.
2. Add a matching entry here.
3. Reflect externally visible changes in `README.md` or `docs/` when needed.

## [Unreleased]

### Changed

- Default application startup no longer creates a datasource automatically and now uses an in-memory sample repository until a persistence profile is explicitly enabled.
- Docker Compose app profile now starts without PostgreSQL or MinIO by default so the boilerplate container can expose the API on its own.
- Docker Compose now manages the `app` service without requiring a separate profile so `up` and `down` behave symmetrically for local runs.
- JSON API endpoints now default to the configurable `app.api.prefix=/rest/v1` route base instead of hardcoded `/api/v1` paths.
- The service now exposes readiness-aware graceful shutdown draining and the container/runtime manifests include health checks for `/health/ready`.
- Readiness health checks now ignore disabled optional integrations instead of marking the starter container unhealthy by default.

## [0.1.0-SNAPSHOT] - 2026-04-14

### Added

- Micronaut starter baseline for HTTP, persistence, storage and document-tool integration points.
- Docker, Docker Compose and Kubernetes starter assets.
- CLI entry commands for `serve`, `version`, `env` and `help`.
- Spotless formatter integration and Makefile workflow.
