# Logging Configuration Design

**Date:** 2026-06-02
**Status:** Approved
**Approach:** Extend logback.xml with Janino conditionals (Approach A)

## Problem

The application logs exclusively via a text-format console appender and an optional JSON file
appender, controlled by `FILE_LOGGING_ENABLED`. There is no way to direct logs to stderr, and
there is no way to switch the console appender to JSON format. This makes the application
unsuitable as a first-class Kubernetes workload, where best practice is to log structured JSON to
stdout so the container runtime and log collectors (Fluent Bit, Loki, ELK) can capture and parse
the output.

## Goals

- Add `LOG_TARGET=stdout|stderr|file` (default: `stdout`)
- Add `LOG_FORMAT=json|text` (default: `text`)
- Add `LOG_FILE` and `LOG_ERROR_FILE` for custom file paths (file target only)
- Kubernetes deployment uses `LOG_TARGET=stdout` + `LOG_FORMAT=json`
- Legacy/on-prem deployments can continue to use `LOG_TARGET=file`
- `kubectl logs` compatibility for stdout target

## Out of Scope

- Syslog or other exotic targets
- Per-logger format overrides
- Log sampling or rate limiting
- Programmatic logback configuration (Approach B was rejected in favour of Approach A)

## Environment Variables

### New Variables

| Variable         | Default                      | Allowed values             | Description                                    |
|------------------|------------------------------|----------------------------|------------------------------------------------|
| `LOG_TARGET`     | `stdout`                     | `stdout`, `stderr`, `file` | Where to write logs                            |
| `LOG_FORMAT`     | `text`                       | `text`, `json`             | Output format                                  |
| `LOG_FILE`       | `${LOG_DIR}/application.log` | any path                   | App log path (file target only)                |
| `LOG_ERROR_FILE` | `${LOG_DIR}/error.log`       | any path                   | Error log path (file target only, ERROR+ only) |

### Retained Variables

| Variable             | Default                    | Description                                                     |
|----------------------|----------------------------|-----------------------------------------------------------------|
| `LOG_LEVEL`          | `INFO`                     | Root log level, unchanged                                       |
| `LOG_DIR`            | `logs`                     | Base directory, default prefix for `LOG_FILE`/`LOG_ERROR_FILE` |
| `LOG_MAX_FILE_SIZE`  | `50MB`                     | Rolling policy, unchanged                                       |
| `LOG_MAX_HISTORY`    | `12`                       | Rolling policy, unchanged                                       |
| `LOG_TOTAL_SIZE_CAP` | `1GB`                      | Rolling policy, unchanged                                       |
| `APP_NAME`           | `java-starter-boilerplate` | Included in every log entry                                     |

### Removed Variables

| Variable                       | Replaced by                                                                      |
|--------------------------------|----------------------------------------------------------------------------------|
| `FILE_LOGGING_ENABLED`         | `LOG_TARGET=file`                                                                |
| `JSON_CONSOLE_LOGGING_ENABLED` | `LOG_FORMAT=json` (was in `.env.example` but never implemented in `logback.xml`) |

## Appender Structure

Eight appenders are defined; the correct combination is activated by nested `<if>` blocks based
on `LOG_TARGET` and `LOG_FORMAT`.

### Console Appenders

| Name          | Target       | Format          |
|---------------|--------------|-----------------|
| `STDOUT_TEXT` | `System.out` | logfmt pattern  |
| `STDOUT_JSON` | `System.out` | LogstashEncoder |
| `STDERR_TEXT` | `System.err` | logfmt pattern  |
| `STDERR_JSON` | `System.err` | LogstashEncoder |

### File Appenders

| Name              | File                | Format          | Filter   |
|-------------------|---------------------|-----------------|----------|
| `APP_FILE_TEXT`   | `${LOG_FILE}`       | logfmt pattern  | none     |
| `APP_FILE_JSON`   | `${LOG_FILE}`       | LogstashEncoder | none     |
| `ERROR_FILE_TEXT` | `${LOG_ERROR_FILE}` | logfmt pattern  | >= ERROR |
| `ERROR_FILE_JSON` | `${LOG_ERROR_FILE}` | LogstashEncoder | >= ERROR |

All file appenders use `SizeAndTimeBasedRollingPolicy` (weekly archive, gzip, governed by
`LOG_MAX_FILE_SIZE`, `LOG_MAX_HISTORY`, `LOG_TOTAL_SIZE_CAP`).

## Activation Logic

```
LOG_TARGET=stdout
  +--> LOG_FORMAT=json  --> root refs: [STDOUT_JSON]
  +--> LOG_FORMAT=text  --> root refs: [STDOUT_TEXT]

LOG_TARGET=stderr
  +--> LOG_FORMAT=json  --> root refs: [STDERR_JSON]
  +--> LOG_FORMAT=text  --> root refs: [STDERR_TEXT]

LOG_TARGET=file
  +--> LOG_FORMAT=json  --> root refs: [APP_FILE_JSON, ERROR_FILE_JSON]
  +--> LOG_FORMAT=text  --> root refs: [APP_FILE_TEXT, ERROR_FILE_TEXT]
```

## Log Formats

### text (logfmt)

Human-readable, compatible with `kubectl logs`:

```
ts=2026-06-02T10:15:30.123Z level=INFO  app=java-starter-boilerplate env=prod thread=main logger=InfoController requestId=abc123 msg="Request received"
```

### json (LogstashEncoder)

Structured JSON, parseable by Fluent Bit / Loki / ELK:

```json
{"@timestamp":"2026-06-02T10:15:30.123Z","level":"INFO","service":"java-starter-boilerplate","environment":"prod","requestId":"abc123","message":"Request received"}
```

## Files Changed

| File                             | Change                                                                        |
|----------------------------------|-------------------------------------------------------------------------------|
| `src/main/resources/logback.xml` | New properties, 8 appenders, nested `<if>` activation                        |
| `.env.example`                   | Replace `FILE_LOGGING_ENABLED`+`JSON_CONSOLE_LOGGING_ENABLED` with new vars   |
| `k8s/configmap.yaml`             | Add `LOG_TARGET=stdout`, `LOG_FORMAT=json`, remove `FILE_LOGGING_ENABLED`     |
| `docs/configuration.md`          | Update logging section                                                        |
| `CHANGELOG.md`                   | Record change                                                                 |

## Testing Strategy

No production Java class changes. Verification:

1. **Existing integration tests** (`ApiHttpTest`, `HealthControllerTest`) start the application
   and exercise it, confirming `logback.xml` loads correctly with defaults
   (`LOG_TARGET=stdout`, `LOG_FORMAT=text`).
2. **Manual smoke test** — `make run` with `LOG_TARGET=stderr LOG_FORMAT=json` etc. to visually
   confirm output routing and format.
3. **`make check`** — full build, tests, 100% coverage gate — confirms no production class
   was broken.

## Kubernetes Usage

`k8s/configmap.yaml` will be updated to:

```yaml
LOG_TARGET: "stdout"
LOG_FORMAT: "json"
LOG_LEVEL: "INFO"
```

Logs are then captured by the container runtime and forwarded by Fluent Bit / Promtail to Loki
or ELK. No volume mounts or file collection needed.

## Backward Compatibility

Legacy deployments that previously used `FILE_LOGGING_ENABLED=true` must migrate to
`LOG_TARGET=file`. The old variable is removed and will have no effect if left in configuration.
