# Configuration

The application uses Micronaut environment-specific YAML files.

Flyway is the single schema authority. Hibernate auto-DDL is disabled, so schema changes belong in migrations, not in entity-side generation.

The project is targeted and verified against Java SE Development Kit 25.x.

## Profiles

- Default: zero-dependency startup with in-memory sample persistence and no datasource.
- `postgresql`: PostgreSQL datasource and Flyway profile.
- `sqlserver`: SQL Server datasource and Flyway profile.
- `test`: H2 test datasource and Flyway test seed.

Activate profiles with `MICRONAUT_ENVIRONMENTS`, for example:

```bash
MICRONAUT_ENVIRONMENTS=dev,postgresql ./gradlew run
MICRONAUT_ENVIRONMENTS=prod,sqlserver ./gradlew run
```

## Persistence

- `PERSISTENCE_ENABLED=false` is the default.
- When persistence is disabled, the application boots without a datasource and serves sample data from an in-memory repository.
- `postgresql` and `sqlserver` profiles set `persistence.enabled=true` and provide datasource, JPA and Flyway configuration.
- `application-test.yaml` keeps H2 enabled so repository and migration tests still verify the JDBC path.

## Storage

`storage.s3.*` maps to a MinIO-compatible adapter built on AWS SDK v2.

Important variables:

- `STORAGE_S3_ENABLED`
- `STORAGE_S3_ENDPOINT`
- `STORAGE_S3_REGION`
- `STORAGE_S3_BUCKET`
- `STORAGE_S3_PATH_STYLE_ACCESS`
- `STORAGE_S3_ACCESS_KEY`
- `STORAGE_S3_SECRET_KEY`

## Document Tooling

- `document.pdfbox.*` controls PDF inspection integration points.
- `document.dss.*` controls DSS placeholder integration points for future PAdES flows.

## Logging

Logging is controlled by two environment variables:

| Variable     | Default  | Values                     | Description                        |
|--------------|----------|----------------------------|------------------------------------|
| `LOG_TARGET` | `stdout` | `stdout`, `stderr`, `file` | Where to write logs                |
| `LOG_FORMAT` | `text`   | `text`, `json`             | Output format                      |

**stdout / stderr** — suitable for Kubernetes and any container runtime. Use
`LOG_FORMAT=json` in production so collectors (Fluent Bit, Loki, ELK) can parse
structured fields.

**file** — writes to `LOG_FILE` (default `logs/application.log`) and
`LOG_ERROR_FILE` (default `logs/error.log`, ERROR level only). Both files use
weekly rolling with gzip compression configured via `LOG_MAX_FILE_SIZE`,
`LOG_MAX_HISTORY`, and `LOG_TOTAL_SIZE_CAP`.

Every log line (both `text` and `json` formats) carries these fields:

| Field | Source |
|-------|--------|
| `service` / `app` | `APP_NAME` |
| `version` | `APP_VERSION` (baked into the image at build time) |
| `environment` | `MICRONAUT_ENVIRONMENTS` |
| `host` | container `HOSTNAME` (the pod name in Kubernetes) |
| `ip` | `POD_IP` (populated via the Kubernetes downward API `status.podIP`; the Helm chart sets this automatically) |
| `level`, `message`, `logger`, `thread` | standard Logback/Logstash fields |
| `requestId` | `X-Request-Id` / `X-Correlation-Id`, propagated via MDC by `RequestIdFilter` |

Full configuration is in `src/main/resources/logback.xml`.

## TLS

| Variable | Default | Description |
|----------|---------|--------------|
| `TLS_ENABLED` | `false` | Enables native Micronaut SSL (PEM certificate) |
| `TLS_CERT_PATH` | `/etc/tls/tls.crt` | Path to the PEM certificate |
| `TLS_KEY_PATH` | `/etc/tls/tls.key` | Path to the PEM private key |

Micronaut 4.x has no dual-protocol support, so enabling TLS switches the entire
server — including `/healthz`, `/readyz` and `/metrics` — to HTTPS on the same
port (`MICRONAUT_SERVER_PORT`, default `8080`). The default paths match a
cert-manager-issued `Secret` (`tls.crt` / `tls.key`) mounted read-only; the
Helm chart's `tls.*` values handle the mount and the matching probe scheme
automatically (see [deployment.md](deployment.md)).

## API Response Envelope

JSON API endpoints return the standard envelope documented in `docs/api-response.md`.

Important keys:

- `app.api.prefix`
- `api.response.problem-base-uri`
- `X-Request-Id` response header
- `X-Correlation-Id` response header as a temporary compatibility alias

Default JSON API route prefix is `/rest/v1`. Override it with `APP_API_PREFIX` if the service needs a different base path.

Validation errors return HTTP `422` with RFC 9457-compatible problem details embedded in the envelope.
Malformed JSON returns HTTP `400`.
Unexpected runtime failures return HTTP `500` with sanitized detail text.

## API Documentation

The application serves generated API documentation from build output:

- `APP_DOCS_ENABLED`
- `APP_DOCS_TITLE`
- `APP_DOCS_SPEC_RESOURCE_PATH`
- `APP_DOCS_OPENAPI_VERSION`
- `APP_DOCS_SCALAR_SCRIPT_URL`

Default routes:

- `/reference`
- `/doc`
- `/doc/openapi.json` as a compatibility alias for `/doc`
- `/doc/openapi.yaml`

## Monitoring (Prometheus)

- `/metrics` exposes Prometheus metrics in text exposition format.
- `micronaut.metrics.enabled=true` enables metrics instrumentation.
- `micronaut.metrics.export.prometheus.enabled=true` enables Prometheus registry export.
- `METRICS_PROMETHEUS_ENABLED=false` disables Prometheus export at runtime.

For stack-level recommendations, see [monitoring.md](monitoring.md).

## Version Source

The canonical project version lives in `gradle.properties` as `projectVersion`.
`app.info.version` defaults to that value unless `APP_VERSION` is set at runtime.

The project follows Semantic Versioning 2.0.0. Keep `projectVersion` as the release identity, for example `1.2.3`, `1.3.0-rc.1` or `0.1.0-SNAPSHOT` during unreleased development. Do not use CI build numbers as the main version number.

Build traceability is configured separately:

- `app.info.build-number` defaults to the Gradle build metadata token and can be overridden with `APP_BUILD_NUMBER`.
- `app.info.build-commit` defaults to the Gradle build metadata token and can be overridden with `APP_BUILD_COMMIT`.
- `app.info.build-timestamp` defaults to the Gradle build metadata token and can be overridden with `APP_BUILD_TIMESTAMP`.

Set the `APP_BUILD_*` runtime overrides only when the deployment pipeline provides real values. Leaving them unset preserves the metadata embedded during the Gradle build.

Gradle reads build metadata from common CI variables:

- build number: `BUILD_NUMBER`, `GITHUB_RUN_NUMBER`, `CI_PIPELINE_IID`
- commit SHA: `GIT_COMMIT`, `GITHUB_SHA`, `CI_COMMIT_SHA`
- timestamp: `BUILD_TIMESTAMP`, `CI_COMMIT_TIMESTAMP`, or the current build time

`GET /rest/v1/info` exposes the resolved values under `data.build`.
