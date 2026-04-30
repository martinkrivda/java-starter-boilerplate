# Development

## Local Java Workflow

Preferred local runtime:

- Java SE Development Kit 25.x

Common commands:

```bash
make doctor
make run
make check
make format
make jar
```

After starting the service locally, API documentation is available at:

```text
http://localhost:8080/
http://localhost:8080/reference
http://localhost:8080/doc
http://localhost:8080/doc/openapi.json
http://localhost:8080/doc/openapi.yaml
```

## Docker-Only Workflow

Use this mode when the development PC does not have Java installed.

Requirements:

- Docker Desktop or Docker Engine
- source code checked out locally

### 1. Start dependencies

```bash
docker compose up -d postgres minio
```

### 2. Run validation

```bash
make docker-check
```

This runs:

- tests
- Checkstyle validation
- ArchUnit architecture tests
- Jacoco coverage verification
- Spotless validation with `google-java-format` for Java sources
- fat JAR packaging

### 3. Apply formatting

```bash
make docker-format
```

### 4. Check dependency updates

```bash
make dependency-updates
```

### 5. Build the executable JAR

```bash
make docker-jar
```

### 6. Run the CLI from the built artifact

```bash
java -jar build/libs/java-starter-boilerplate-<version>-all.jar help
```

If the host also lacks a JVM for the final execution step, run the service container instead:

```bash
docker compose up -d app
```

## Notes

- Docker-based Gradle commands use `eclipse-temurin:25-jdk`.
- The repository is mounted into the container, so generated files appear in the local working tree.
- This approach is intended for development and CI-like verification, not for replacing the production image.

## Cleanup

Use these commands when you want to remove everything created for local development:

```bash
make clean-deep
make docker-clean
make reset-workspace
```

Meaning:

- `make clean-deep` removes local build outputs and local Gradle state:
  - `build/`
  - `logs/`
  - `.gradle/`
  - `.gradle-wrapper/`
- `make docker-clean` removes:
  - Compose containers
  - Compose networks
  - Compose volumes
  - the local application image
  - pulled helper images used by this project
  - Docker builder cache
- `make reset-workspace` runs both groups

Important note:

- The temporary `apt-get` packages installed inside the disposable Gradle container are not stored on the host. They disappear when the container exits.
- The persistent parts are Docker images, Docker volumes, Docker build cache and generated files in the repository workspace.
