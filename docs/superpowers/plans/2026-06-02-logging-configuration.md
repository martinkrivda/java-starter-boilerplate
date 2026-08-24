# Logging Configuration (LOG_TARGET / LOG_FORMAT) Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the `FILE_LOGGING_ENABLED` boolean toggle with `LOG_TARGET=stdout|stderr|file` and `LOG_FORMAT=json|text` so the application can log structured JSON to stdout for Kubernetes while retaining file logging for legacy deployments.

**Architecture:** Pure logback.xml change — 8 named appenders (4 console, 4 file) activated by nested Janino `<if>` conditions on `LOG_TARGET` and `LOG_FORMAT`. No production Java class changes. Existing integration tests verify the configuration loads correctly.

**Tech Stack:** Logback 1.x + Janino (already on classpath), logstash-logback-encoder (already a dependency), Gradle `./gradlew check` as the verification gate.

---

## File Map

| Action | File |
|--------|------|
| Modify | `src/main/resources/logback.xml` |
| Modify | `.env.example` |
| Modify | `k8s/configmap.yaml` |
| Modify | `docs/configuration.md` |
| Modify | `CHANGELOG.md` |

---

## Task 1 — Rewrite logback.xml

Replace the entire file with 8 appenders and nested `<if>` activation logic.

**Files:**
- Modify: `src/main/resources/logback.xml`

- [ ] **Step 1.1: Replace logback.xml with the new configuration**

Write the following complete content to `src/main/resources/logback.xml`:

```xml
<configuration scan="true" scanPeriod="60 seconds">
    <property name="APP_NAME" value="${APP_NAME:-java-starter-boilerplate}" />
    <property name="APP_ENV" value="${MICRONAUT_ENVIRONMENTS:-default}" />
    <property name="LOG_DIR" value="${LOG_DIR:-logs}" />
    <property name="LOG_LEVEL" value="${LOG_LEVEL:-INFO}" />
    <property name="LOG_TARGET" value="${LOG_TARGET:-stdout}" />
    <property name="LOG_FORMAT" value="${LOG_FORMAT:-text}" />
    <property name="LOG_FILE" value="${LOG_FILE:-${LOG_DIR}/application.log}" />
    <property name="LOG_ERROR_FILE" value="${LOG_ERROR_FILE:-${LOG_DIR}/error.log}" />
    <property name="LOG_MAX_HISTORY" value="${LOG_MAX_HISTORY:-12}" />
    <property name="LOG_TOTAL_SIZE_CAP" value="${LOG_TOTAL_SIZE_CAP:-1GB}" />
    <property name="LOG_MAX_FILE_SIZE" value="${LOG_MAX_FILE_SIZE:-50MB}" />

    <!-- ── Console: stdout + text ─────────────────────────────────────── -->
    <appender name="STDOUT_TEXT" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.out</target>
        <encoder>
            <pattern>ts=%d{yyyy-MM-dd'T'HH:mm:ss.SSSX} level=%-5level app=${APP_NAME} env=${APP_ENV} thread=%thread logger=%logger{36} requestId=%X{requestId:-na} msg="%msg"%n</pattern>
        </encoder>
    </appender>

    <!-- ── Console: stdout + json ─────────────────────────────────────── -->
    <appender name="STDOUT_JSON" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.out</target>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${APP_NAME}","environment":"${APP_ENV}"}</customFields>
        </encoder>
    </appender>

    <!-- ── Console: stderr + text ─────────────────────────────────────── -->
    <appender name="STDERR_TEXT" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.err</target>
        <encoder>
            <pattern>ts=%d{yyyy-MM-dd'T'HH:mm:ss.SSSX} level=%-5level app=${APP_NAME} env=${APP_ENV} thread=%thread logger=%logger{36} requestId=%X{requestId:-na} msg="%msg"%n</pattern>
        </encoder>
    </appender>

    <!-- ── Console: stderr + json ─────────────────────────────────────── -->
    <appender name="STDERR_JSON" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.err</target>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${APP_NAME}","environment":"${APP_ENV}"}</customFields>
        </encoder>
    </appender>

    <!-- ── File: application.log + text ──────────────────────────────── -->
    <appender name="APP_FILE_TEXT" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder>
            <pattern>ts=%d{yyyy-MM-dd'T'HH:mm:ss.SSSX} level=%-5level app=${APP_NAME} env=${APP_ENV} thread=%thread logger=%logger{36} requestId=%X{requestId:-na} msg="%msg"%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/archive/application.%d{yyyy-ww}.%i.log.gz</fileNamePattern>
            <maxHistory>${LOG_MAX_HISTORY}</maxHistory>
            <maxFileSize>${LOG_MAX_FILE_SIZE}</maxFileSize>
            <totalSizeCap>${LOG_TOTAL_SIZE_CAP}</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <!-- ── File: application.log + json ──────────────────────────────── -->
    <appender name="APP_FILE_JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_FILE}</file>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${APP_NAME}","environment":"${APP_ENV}","stream":"application"}</customFields>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/archive/application.%d{yyyy-ww}.%i.log.gz</fileNamePattern>
            <maxHistory>${LOG_MAX_HISTORY}</maxHistory>
            <maxFileSize>${LOG_MAX_FILE_SIZE}</maxFileSize>
            <totalSizeCap>${LOG_TOTAL_SIZE_CAP}</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <!-- ── File: error.log + text ────────────────────────────────────── -->
    <appender name="ERROR_FILE_TEXT" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_ERROR_FILE}</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder>
            <pattern>ts=%d{yyyy-MM-dd'T'HH:mm:ss.SSSX} level=%-5level app=${APP_NAME} env=${APP_ENV} thread=%thread logger=%logger{36} requestId=%X{requestId:-na} msg="%msg"%n</pattern>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/archive/error.%d{yyyy-ww}.%i.log.gz</fileNamePattern>
            <maxHistory>${LOG_MAX_HISTORY}</maxHistory>
            <maxFileSize>${LOG_MAX_FILE_SIZE}</maxFileSize>
            <totalSizeCap>${LOG_TOTAL_SIZE_CAP}</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <!-- ── File: error.log + json ────────────────────────────────────── -->
    <appender name="ERROR_FILE_JSON" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>${LOG_ERROR_FILE}</file>
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder class="net.logstash.logback.encoder.LogstashEncoder">
            <customFields>{"service":"${APP_NAME}","environment":"${APP_ENV}","stream":"error"}</customFields>
        </encoder>
        <rollingPolicy class="ch.qos.logback.core.rolling.SizeAndTimeBasedRollingPolicy">
            <fileNamePattern>${LOG_DIR}/archive/error.%d{yyyy-ww}.%i.log.gz</fileNamePattern>
            <maxHistory>${LOG_MAX_HISTORY}</maxHistory>
            <maxFileSize>${LOG_MAX_FILE_SIZE}</maxFileSize>
            <totalSizeCap>${LOG_TOTAL_SIZE_CAP}</totalSizeCap>
            <cleanHistoryOnStart>true</cleanHistoryOnStart>
        </rollingPolicy>
    </appender>

    <logger name="com.example.javastarterboilerplate" level="${LOG_LEVEL}" />

    <!-- ── Activate root based on LOG_TARGET + LOG_FORMAT ────────────── -->
    <if condition='property("LOG_TARGET").equalsIgnoreCase("stderr")'>
        <then>
            <if condition='property("LOG_FORMAT").equalsIgnoreCase("json")'>
                <then>
                    <root level="${LOG_LEVEL}">
                        <appender-ref ref="STDERR_JSON" />
                    </root>
                </then>
                <else>
                    <root level="${LOG_LEVEL}">
                        <appender-ref ref="STDERR_TEXT" />
                    </root>
                </else>
            </if>
        </then>
        <else>
            <if condition='property("LOG_TARGET").equalsIgnoreCase("file")'>
                <then>
                    <if condition='property("LOG_FORMAT").equalsIgnoreCase("json")'>
                        <then>
                            <root level="${LOG_LEVEL}">
                                <appender-ref ref="APP_FILE_JSON" />
                                <appender-ref ref="ERROR_FILE_JSON" />
                            </root>
                        </then>
                        <else>
                            <root level="${LOG_LEVEL}">
                                <appender-ref ref="APP_FILE_TEXT" />
                                <appender-ref ref="ERROR_FILE_TEXT" />
                            </root>
                        </else>
                    </if>
                </then>
                <else>
                    <!-- Default: stdout (covers "stdout" and any unrecognised value) -->
                    <if condition='property("LOG_FORMAT").equalsIgnoreCase("json")'>
                        <then>
                            <root level="${LOG_LEVEL}">
                                <appender-ref ref="STDOUT_JSON" />
                            </root>
                        </then>
                        <else>
                            <root level="${LOG_LEVEL}">
                                <appender-ref ref="STDOUT_TEXT" />
                            </root>
                        </else>
                    </if>
                </else>
            </if>
        </else>
    </if>
</configuration>
```

- [ ] **Step 1.2: Run tests to verify logback.xml loads with defaults**

```bash
./gradlew test
```

Expected: All tests pass. The integration tests (`ApiHttpTest`, `HealthControllerTest`) start the full Micronaut context, which triggers logback initialisation with `LOG_TARGET=stdout` and `LOG_FORMAT=text` defaults.

If you see a logback error like `Failed to instantiate [ch.qos.logback.classic.LoggerContext]` or `janino` not found, check that `janino` is on the classpath (it is already present — the original logback.xml used `<if>` as well).

- [ ] **Step 1.3: Commit**

```bash
git add src/main/resources/logback.xml
git commit -m "feat: replace FILE_LOGGING_ENABLED with LOG_TARGET and LOG_FORMAT

Adds LOG_TARGET=stdout|stderr|file (default: stdout) and LOG_FORMAT=json|text
(default: text). Removes FILE_LOGGING_ENABLED and JSON_CONSOLE_LOGGING_ENABLED."
```

---

## Task 2 — Update .env.example

Replace removed variables with the new ones.

**Files:**
- Modify: `.env.example`

- [ ] **Step 2.1: Edit the logging block in .env.example**

Find the existing logging block (currently contains `FILE_LOGGING_ENABLED` and
`JSON_CONSOLE_LOGGING_ENABLED`) and replace it with:

```
# Logging
# Where to write logs: stdout | stderr | file
LOG_TARGET=stdout
# Log output format: text (logfmt, human-readable) | json (structured, for log collectors)
LOG_FORMAT=text
# Root log level
LOG_LEVEL=INFO
# Base directory for file logs (used when LOG_TARGET=file)
LOG_DIR=logs
# Override log file paths (defaults to LOG_DIR/application.log and LOG_DIR/error.log)
LOG_FILE=logs/application.log
LOG_ERROR_FILE=logs/error.log
# File rolling policy (used when LOG_TARGET=file)
LOG_MAX_FILE_SIZE=50MB
LOG_MAX_HISTORY=12
LOG_TOTAL_SIZE_CAP=1GB
```

- [ ] **Step 2.2: Commit**

```bash
git add .env.example
git commit -m "docs: update .env.example for LOG_TARGET and LOG_FORMAT"
```

---

## Task 3 — Update k8s/configmap.yaml

Reflect the Kubernetes-first defaults: stdout + JSON.

**Files:**
- Modify: `k8s/configmap.yaml`

- [ ] **Step 3.1: Remove FILE_LOGGING_ENABLED, add LOG_TARGET and LOG_FORMAT**

In `k8s/configmap.yaml`, find:

```yaml
  FILE_LOGGING_ENABLED: "false"
  LOG_LEVEL: "INFO"
```

Replace with:

```yaml
  LOG_TARGET: "stdout"
  LOG_FORMAT: "json"
  LOG_LEVEL: "INFO"
```

- [ ] **Step 3.2: Commit**

```bash
git add k8s/configmap.yaml
git commit -m "ops: configure LOG_TARGET=stdout LOG_FORMAT=json for Kubernetes"
```

---

## Task 4 — Update docs/configuration.md

Replace the outdated logging section.

**Files:**
- Modify: `docs/configuration.md`

- [ ] **Step 4.1: Replace the Logging section**

In `docs/configuration.md`, find the `## Logging` section (currently lines ~49–53):

```markdown
## Logging

- Console logging is always on.
- File logging is enabled with `FILE_LOGGING_ENABLED=true`.
- Weekly rolling, retention and gzip compression are configured in `src/main/resources/logback.xml`.
```

Replace with:

```markdown
## Logging

Logging is controlled by two environment variables:

| Variable     | Default  | Values                   | Description                        |
|--------------|----------|--------------------------|------------------------------------|
| `LOG_TARGET` | `stdout` | `stdout`, `stderr`, `file` | Where to write logs              |
| `LOG_FORMAT` | `text`   | `text`, `json`           | Output format                      |

**stdout / stderr** — suitable for Kubernetes and any container runtime. Use
`LOG_FORMAT=json` in production so collectors (Fluent Bit, Loki, ELK) can parse
structured fields.

**file** — writes to `LOG_FILE` (default `logs/application.log`) and
`LOG_ERROR_FILE` (default `logs/error.log`, ERROR level only). Both files use
weekly rolling with gzip compression configured via `LOG_MAX_FILE_SIZE`,
`LOG_MAX_HISTORY`, and `LOG_TOTAL_SIZE_CAP`.

Full configuration is in `src/main/resources/logback.xml`.
```

- [ ] **Step 4.2: Commit**

```bash
git add docs/configuration.md
git commit -m "docs: update logging section for LOG_TARGET and LOG_FORMAT"
```

---

## Task 5 — Update CHANGELOG.md

**Files:**
- Modify: `CHANGELOG.md`

- [ ] **Step 5.1: Add changelog entry**

In `CHANGELOG.md`, prepend a new entry under `## [Unreleased]` (or the topmost
version section):

```markdown
### Changed
- Replaced `FILE_LOGGING_ENABLED` with `LOG_TARGET=stdout|stderr|file` (default: `stdout`)
- Added `LOG_FORMAT=json|text` (default: `text`) for configurable output format
- Added `LOG_FILE` and `LOG_ERROR_FILE` for custom file paths when `LOG_TARGET=file`
- Removed never-implemented `JSON_CONSOLE_LOGGING_ENABLED` variable
- Kubernetes configmap now sets `LOG_TARGET=stdout` and `LOG_FORMAT=json`
```

- [ ] **Step 5.2: Commit**

```bash
git add CHANGELOG.md
git commit -m "docs: changelog entry for LOG_TARGET / LOG_FORMAT feature"
```

---

## Task 6 — Final verification

- [ ] **Step 6.1: Run the full check**

```bash
./gradlew check
```

Expected output (all must pass):
```
> Task :spotlessCheck UP-TO-DATE  (or SKIPPED)
> Task :test
  Tests run: N, Failures: 0, Errors: 0, Skipped: 0
> Task :jacocoTestCoverageVerification PASSED
> Task :check PASSED
BUILD SUCCESSFUL
```

If `spotlessCheck` fails, run `./gradlew spotlessApply` and re-commit the formatted files.

- [ ] **Step 6.2: Spot-check log output manually**

```bash
# Default: stdout, text
./gradlew run &
sleep 5 && curl -s http://localhost:8080/health/live
# Expect logfmt lines on stdout: ts=... level=INFO ...

# JSON on stdout
LOG_FORMAT=json ./gradlew run &
sleep 5 && curl -s http://localhost:8080/health/live
# Expect JSON lines: {"@timestamp":"...","level":"INFO",...}

# stderr routing
LOG_TARGET=stderr ./gradlew run 2>/tmp/stderr.log &
sleep 5 && curl -s http://localhost:8080/health/live
cat /tmp/stderr.log
# Expect logfmt lines in /tmp/stderr.log

kill %1 2>/dev/null; kill %2 2>/dev/null; kill %3 2>/dev/null
```
