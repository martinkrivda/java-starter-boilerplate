# Architecture

`java-starter-boilerplate` is a single-module Micronaut service with explicit package boundaries:

- `api`: HTTP controllers and DTOs.
- `api.response`: shared response envelope, metadata and problem details contracts.
- `application`: orchestration without transport or vendor details.
- `domain`: framework-free contracts and core models.
- `infrastructure.persistence`: Micronaut Data, JPA entities, Flyway integration points.
- `infrastructure.storage`: S3/MinIO adapter and client factory.
- `infrastructure.document`: PDFBox and DSS integration points.
- `config`: cross-cutting configuration beans.
- `observability`: request correlation and runtime visibility helpers.

## Extension Rules

- Keep business workflows out of controllers and infrastructure adapters.
- Add sealing/signing orchestration in `application`.
- Keep vendor SDKs and concrete libraries in `infrastructure`.
- Prefer explicit records and small services over deep abstraction trees.
- Keep JSON REST responses inside the standard envelope unless the endpoint intentionally serves a raw artifact such as OpenAPI JSON/YAML or binary content.
