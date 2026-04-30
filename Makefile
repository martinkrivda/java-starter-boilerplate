SHELL := /bin/sh
GRADLE := ./gradlew
JAVA_REQUIRED_MAJOR := 25
APP_JAR := build/libs/java-starter-boilerplate-$(shell sed -n 's/^projectVersion=//p' gradle.properties)-all.jar
DOCKER_GRADLE_IMAGE := eclipse-temurin:25-jdk
DOCKER_GRADLE_CMD := docker run --rm -v "$(CURDIR):/workspace" -w /workspace $(DOCKER_GRADLE_IMAGE) bash -lc 'apt-get update >/dev/null && apt-get install -y --no-install-recommends curl unzip >/dev/null && $(GRADLE) --no-daemon
DOCKER_GRADLE_END := '

.PHONY: help doctor run run-postgresql run-sqlserver test check format format-check dependency-updates jar cli-help cli-version cli-env docker-build compose-up compose-down compose-logs docker-test docker-check docker-format docker-jar docker-clean clean clean-deep reset-workspace

help:
	@printf "Common targets:\n"
	@printf "  make doctor          Verify local Java runtime\n"
	@printf "  make run             Start the service with the default profile\n"
	@printf "  make run-postgresql  Start the service with the PostgreSQL profile\n"
	@printf "  make run-sqlserver   Start the service with the SQL Server profile\n"
	@printf "  make test            Run tests\n"
	@printf "  make check           Run checks including coverage and formatter validation\n"
	@printf "  make format          Apply Spotless formatting\n"
	@printf "  make dependency-updates Show available Gradle dependency updates\n"
	@printf "  make jar             Build the executable fat JAR\n"
	@printf "  make docker-check    Run checks in Docker without local Java\n"
	@printf "  make docker-format   Run formatter in Docker without local Java\n"
	@printf "  make docker-jar      Build the fat JAR in Docker without local Java\n"
	@printf "  make docker-clean    Remove compose containers, volumes and pulled Docker images\n"
	@printf "  make clean-deep      Remove local build and Gradle workspace artifacts\n"
	@printf "  make reset-workspace Remove local artifacts and Docker resources created for this project\n"
	@printf "  make cli-help        Print CLI help from the packaged application\n"
	@printf "  make compose-up      Start local dependencies via Docker Compose\n"
	@printf "  make compose-down    Stop Docker Compose services\n"

doctor:
	@java -version 2>&1 | grep 'version "$(JAVA_REQUIRED_MAJOR)\.' >/dev/null || { \
		printf "Expected Java SE Development Kit %s.x.\n" "$(JAVA_REQUIRED_MAJOR)"; \
		java -version; \
		exit 1; \
	}
	@printf "Java runtime matches major version %s.\n" "$(JAVA_REQUIRED_MAJOR)"

run:
	$(GRADLE) run

run-postgresql:
	MICRONAUT_ENVIRONMENTS=dev,postgresql $(GRADLE) run

run-sqlserver:
	MICRONAUT_ENVIRONMENTS=dev,sqlserver $(GRADLE) run

test:
	$(GRADLE) test

check:
	$(GRADLE) clean check spotlessCheck

format:
	$(GRADLE) spotlessApply

format-check:
	$(GRADLE) spotlessCheck

dependency-updates:
	$(GRADLE) --no-parallel dependencyUpdates

jar:
	$(GRADLE) clean fatJar

cli-help: jar
	java -jar $(APP_JAR) help

cli-version: jar
	java -jar $(APP_JAR) version

cli-env: jar
	java -jar $(APP_JAR) env

docker-build:
	docker build -t java-starter-boilerplate:latest .

docker-test:
	@$(DOCKER_GRADLE_CMD) test$(DOCKER_GRADLE_END)

docker-check:
	@$(DOCKER_GRADLE_CMD) clean check fatJar$(DOCKER_GRADLE_END)

docker-format:
	@$(DOCKER_GRADLE_CMD) spotlessApply$(DOCKER_GRADLE_END)

docker-jar:
	@$(DOCKER_GRADLE_CMD) clean fatJar$(DOCKER_GRADLE_END)

compose-up:
	docker compose up -d postgres minio

compose-down:
	docker compose down

compose-logs:
	docker compose logs -f

clean:
	$(GRADLE) clean

clean-deep:
	rm -rf build logs .gradle .gradle-wrapper

docker-clean:
	docker compose down --volumes --remove-orphans --rmi local
	-docker image rm $(DOCKER_GRADLE_IMAGE) postgres:17-alpine minio/minio:RELEASE.2026-03-27T21-04-51Z >/dev/null 2>&1 || true
	-docker builder prune -f >/dev/null 2>&1 || true

reset-workspace: clean-deep docker-clean
