# CLAUDE.md

Guidance for Claude Code when working in this repository. Read this before any non-trivial change.

## 1. Project overview

Production-ready **Micronaut** (not Spring Boot) starter for a future **document sealing and signing microservice**. At this stage the repository contains only boilerplate, technical foundations and integration points.

Built with **Java 25**, **Gradle (Kotlin DSL)**, and a layered architecture with strict boundary enforcement.

### Out of scope in this phase

Do not implement, scaffold, or "prepare hooks for":

If a request implies any of the above, stop and confirm scope with the user before writing code. The current focus is technical infrastructure and architectural patterns.

## 2. Tech stack

- **Java**: Oracle JDK **25.0.2 LTS**, Gradle Java toolchains with `languageVersion = 25`
- **Build**: Gradle, **Kotlin DSL**
- **Framework**: Micronaut
- **Style**: Eclipse JDT formatter via **Spotless** (`config/spotless/eclipse-java-formatter.xml`)
- **Persistence**: Hibernate / JPA via Micronaut Data; **H2** (default/test), **PostgreSQL**, **SQL Server**
- **Migrations**: **Flyway** (single source of truth — Hibernate DDL auto is disabled)
- **API**: REST; OpenAPI generated at compile time by Micronaut OpenAPI
- **Docs UI**: Scalar at `GET /doc`
- **Testing**: JUnit 5 + Micronaut Test + Mockito + AssertJ
- **Coverage**: Jacoco with **100% line coverage gate** (only `Application.java` bootstrap is excluded)
- **Ops**: GitHub + GitLab, Kubernetes, ArgoCD, kubectl

### Java rules

- Target Java 25 LTS only.
- Do not enable preview features unless explicitly requested.
- Use only stable Java 25 language features.
- Keep code compatible with production JVM execution (no experimental flags).

## 3. Common commands

### Make (preferred)

```bash
# Run locally (requires Java 25.0.2)
make run                    # H2 (default profile)
make run-postgresql         # PostgreSQL
make run-sqlserver          # SQL Server

# Quality checks (run before committing)
make check                  # Tests + 100% coverage gate + Spotless check
make format                 # Apply Spotless formatting
make test                   # Tests only

# Build
make jar                    # Executable fat JAR

# Docker-based workflows (no local Java required)
make docker-check
make docker-format
make docker-jar

# Cleanup
make clean-deep             # Build artifacts + Gradle state
make docker-clean           # Containers / volumes / images
```

### Gradle equivalents

```bash
./gradlew clean check           # Full build + tests + coverage + format check
./gradlew test                  # Tests only
./gradlew spotlessApply         # Apply formatting
./gradlew spotlessCheck         # Verify formatting
./gradlew clean fatJar          # Build fat JAR
./gradlew run                   # Run with default profile
./gradlew jacocoTestReport      # Coverage report
./gradlew jacocoTestCoverageVerification
```

### Single test

```bash
./gradlew test --tests "com.example.javastarterboilerplate.SomeTest"
./gradlew test --tests "com.example.javastarterboilerplate.SomeTest.methodName"
```

## 4. Architecture

### Package layers

```
com.example.javastarterboilerplate/
├── api/             HTTP only — controllers, DTOs, ApiResponse<T> envelope, GlobalErrorHandler
├── application/     Business orchestration — service classes, no framework annotations beyond @Singleton
├── domain/          Framework-free — interfaces/contracts (Repository, ObjectStorage), records as models
├── infrastructure/  Vendor adapters — JPA entities, Micronaut Data repos, S3, PDFBox, DSS, Flyway
├── config/          Cross-cutting Micronaut @Factory / @ConfigurationProperties beans
└── observability/   RequestIdFilter (X-Request-Id header + MDC), metrics
```

### Layering rules (hard)

- `domain` has **zero** framework imports — no Micronaut, no JPA/Hibernate, no cloud SDK, no vendor SDK.
- `application` depends on `domain` only.
- `api` depends on `application` and `domain`.
- `infrastructure` implements `domain` contracts — nothing else depends on it directly.
- `config` wires `application` and `infrastructure`.
- Controllers are thin: parse, delegate, return `ApiResponse<T>`. No business logic in controllers.
- Vendor SDKs and framework-specific code live only in `infrastructure`.

Enforce these rules with **ArchUnit**-style tests where practical.

### Key infrastructure adapters

| Contract (domain)            | Adapter (infrastructure)                                           |
| ---------------------------- | ------------------------------------------------------------------ |
| `ObjectStorage`              | `S3ObjectStorageAdapter` (S3 / MinIO)                              |
| `PdfDocumentService`         | `PdfBoxDocumentService`                                            |
| `DigitalSignatureService`    | `DssDigitalSignatureService`                                       |
| `SampleDocumentRepository`   | `SampleDocumentPersistenceAdapter` → `SampleDocumentJpaRepository` |

## 5. REST API

### Response envelope

All JSON endpoints return `ApiResponse<T>`:

```json
{
  "success": true,
  "data": { ... },
  "error": null,
  "meta": { "requestId": "...", "timestamp": "..." }
}
```

Errors follow **RFC 9457** via `ApiProblemDetails`, centralized in `GlobalErrorHandler`. Never return raw framework exceptions or ad-hoc error shapes from controllers.

`/health/**` and `/metrics` are **not** wrapped.

### Documentation endpoints

- Scalar UI: `GET /doc`
- OpenAPI JSON: `GET /doc/openapi.json`
- OpenAPI YAML: `GET /doc/openapi.yaml`

OpenAPI is generated at compile time. Do not duplicate generation manually. If the documentation UI references a different filename than what is actually generated, fix the mismatch (do not duplicate the file).

## 6. Database and migrations

- **Flyway is the single source of truth for schema.** Hibernate DDL auto is disabled.
- Migration locations:
  - `src/main/resources/db/migration/common/` — portable SQL
  - `.../h2/`, `.../postgresql/`, `.../sqlserver/` — vendor-specific
- Profiles: `h2` (default and test), `postgresql`, `sqlserver` — switched via Micronaut environments.
- Hibernate / JPA code lives in `infrastructure`.
- Domain objects must not depend on JPA annotations unless explicitly approved.
- Repository contracts are in `domain` / `application`; implementations in `infrastructure`.
- Use environment variables for deploy-time secrets and infrastructure values. Never hardcode credentials, JDBC URLs or secrets.

## 7. Configuration and profiles

- Environment variables defined with defaults in `application.yaml`; full list in `.env.example`.
- Profile-specific overrides: `application-{profile}.yaml`.
- `APP_VERSION` is injected from `gradle.properties` at build time via `@projectVersion@` token substitution.
- Feature toggles for adapters: `STORAGE_S3_ENABLED`, `PDFBOX_ENABLED`, `DSS_ENABLED`.
- Test profile: `application-test.yaml` uses H2 in-memory DB automatically.

## 8. Testing

### Framework

JUnit 5 + Micronaut Test (`@MicronautTest`) + Mockito + AssertJ.

### Required coverage

- controllers (HTTP-level via `@Client`)
- application services
- repository contracts
- storage adapter contracts
- Hibernate mappings
- configuration changes
- OpenAPI `/doc`, `/doc/openapi.json`, `/doc/openapi.yaml`
- architecture / package boundaries (ArchUnit)

### Coverage gate

**100% line coverage on all production code**, only `Application.java` bootstrap excluded. The `./gradlew check` task enforces this — the build fails if coverage drops below 100%.

Any new exclusion must be justified and explicitly documented in the Jacoco config.

Reports: `build/reports/jacoco/test/html/index.html`

### Rules

- Tests must be deterministic and readable. No `Thread.sleep`, no time-of-day branches, no flaky network.
- For DB tests prefer Testcontainers (PostgreSQL, SQL Server) over embedded H2 when the contract differs from H2.
- Never claim tests passed unless they were actually run; if they cannot be run, say why.

## 9. Code conventions

- Prefer **records** for domain models and DTOs.
- Keep classes small and focused; **composition over inheritance**.
- **No premature abstractions** — add only what the current task requires. No speculative interfaces, no dead extension points, no unused factories.
- Readable names over clever names.
- Comments only when they materially improve clarity.

### Commits and branches

- **Conventional Commits**: `feat:`, `fix:`, `chore:`, `docs:`, `refactor:`, `test:`
- **Branch prefixes**: `feature/`, `bugfix/`, `hotfix/`, `release/`

## 10. Code formatting

**Spotless with Eclipse JDT** handles Java formatting (`config/spotless/eclipse-java-formatter.xml`).

- Spotless is the formatting authority. Do not fight it; do not hand-format.
- Run `make format` (or `./gradlew spotlessApply`) before committing.
- CI runs `spotlessCheck` and fails on unformatted code.

## 11. Dependencies

- Centralize versions in **Gradle version catalogs** (`gradle/libs.versions.toml`) when practical.
- Prefer stable, mainstream libraries.
- No dependencies without a concrete reason tied to the starter scope.
- Do not add libraries for convenience when JDK / Micronaut / existing dependencies are sufficient.

## 12. Versioning and documentation

- `gradle.properties` `projectVersion` is the canonical version source.
- Update `CHANGELOG.md` with every version change.
- **Never** create release tags or publish artifacts unless explicitly requested.
- Update `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, `docs/` whenever behavior, public API, infrastructure or configuration changes.
- Document important architectural decisions close to the code or in `docs/` (ADRs).

## 13. CI parity (GitHub / GitLab)

Keep GitHub Actions and GitLab CI behavior aligned. CI must run:

- formatting check (`spotlessCheck`)
- unit tests
- integration tests where practical
- Jacoco verification (100% gate)
- architecture tests
- build (`check` / `fatJar`)

Do not introduce CI-only behavior that cannot be reproduced locally — the `make check` and `make docker-check` targets must stay representative.

## 14. DevOps safety

The repository contains GitHub, GitLab, Kubernetes and ArgoCD assets.

### Safe by default (no approval needed)

- Inspect files inside the repository.
- Read-only Git: `git status`, `git diff`, `git log --oneline`.
- Run tests and formatting checks.
- Render or validate manifests locally (`kubectl --dry-run=client`, `helm template`, `helm lint`).

### Require explicit user approval

- `git push`, creating tags, publishing releases, deleting branches, force-pushing
- `kubectl apply`, `kubectl delete`, `kubectl patch`, `kubectl scale`, `kubectl rollout restart`
- ArgoCD `sync`, `rollback`, `app delete`
- changing live cluster resources
- modifying production secrets
- destructive database migrations (including ad-hoc Flyway operations against shared DBs)
- `docker push` and registry publishing

**Never** assume the active Kubernetes context is safe. Always inspect the current context and namespace (`kubectl config current-context`, `kubectl config view --minify`) before suggesting cluster-changing commands.

## 15. Claude Code behavior

When working on this repo, Claude Code must:

- Prefer **small, reviewable diffs**.
- **Explain the root cause** before broad rewrites.
- **Read existing conventions** (style, package layout, naming) before editing.
- Avoid speculative architecture and unused extension points.
- **Avoid touching unrelated files.**
- After changes, run the most relevant verification command — typically `make check` or a narrower `./gradlew test --tests "..."`.
- Report exactly **what changed** and **what was verified**.
- Ask before destructive operations.
- Never claim tests passed unless they were actually run; if they cannot run, explain why.

## 16. Security and workspace isolation

**Hard requirement.** Claude Code operates only inside this repository workspace.

### Workspace boundaries

- Treat the repository root as the only allowed workspace.
- Do not read, write, edit, create, delete or inspect files outside the repository.
- Do not access `~`, Desktop, Downloads, Documents, SSH keys, shell history, browser data, password stores or any unrelated local files.
- Do not use `..` except to confirm the repo root (no file content read).
- Do not follow symlinks pointing outside the repository.
- Do not scan the whole filesystem.
- Forbidden examples: `find /`, `grep -R /`, `ls ~`, `cat ~/.ssh/*`, `cat ~/.env`.
- Do not read or modify global configs:
  - `~/.gitconfig`, `~/.ssh/*`, `~/.aws/*`, `~/.azure/*`, `~/.kube/config`
  - `~/.docker/config.json`, `~/.m2/settings.xml`, `~/.gradle/gradle.properties`
  - `~/.zshrc`, `~/.bashrc`, `~/.profile`

### Secrets

- Never read, print, copy, summarize or modify secrets.
- Do not open: `.env`, `.env.*`, `*.key`, `*.pem`, `*.p12`, `*.jks`, `id_rsa`, `id_ed25519`, kubeconfigs, cloud credentials, production secret manifests.
- `.env.example` is the **only** env file Claude may read — it is the documented template.
- If a secret file appears relevant, **stop and ask** the user for a sanitized example.
- Never paste secret values into logs, docs, generated code, tests or commit messages.
- Use placeholders such as `<REDACTED>` or `${ENV_VAR_NAME}`.

### Destructive operations

Never run without explicit user approval:

- `rm -rf`, `sudo`, `chmod -R`, `chown -R`
- broad `mv` over directories
- `git reset --hard`, `git clean -fdx`, `git checkout -- .`, `git push --force`
- database drop / truncate / reset commands
- commands that delete Kubernetes or cloud resources
- production migrations or rollback scripts (including Flyway `clean` / `migrate` / `repair` against non-local databases)

Do not modify Git history. Do not delete branches, tags, releases, artifacts, database data, cluster resources or container images.

### Network and external systems

- No external service calls unless explicitly required.
- Never send repository content, source, credentials, logs or config to external URLs.
- No package publishing.
- No `docker push`.
- No deploys to Kubernetes.
- No ArgoCD `sync` / `rollback` / `delete`.
- No `kubectl apply | delete | patch | scale | rollout restart` or equivalent cluster-changing commands.
- Read-only inspection commands are acceptable only after verifying scope and safety.

### Allowed by default

- Reading files inside the repo (excluding secrets).
- Editing files inside the repo (excluding secrets).
- Safe local commands: `pwd`, `git status`, `git diff`, `git log --oneline -n 20`, `make check | format | test`, `./gradlew spotlessCheck | test | check | jacocoTestReport`.
- Creating new files inside the repo when needed for the requested task.

### Before running commands

- Explain the purpose of any command that may modify files.
- Prefer dry-run / validation / read-only modes first.
- Use the narrowest possible path scope.
- Never use broad globs on destructive commands.
- Never run commands from outside the repo root.

### When unsure

- Stop and ask for confirmation.
- Prefer read-only inspection.
- Prefer producing a patch or instructions over executing a risky command.
