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

- Console logging is always on.
- File logging is enabled with `FILE_LOGGING_ENABLED=true`.
- Weekly rolling, retention and gzip compression are configured in `src/main/resources/logback.xml`.

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
