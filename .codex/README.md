# Codex Configuration for Java 25 Micronaut Microservice

This directory contains project-scoped Codex configuration, agents, rules, prompts and helper scripts.

## What is included

- `config.toml` — project model, sandbox, approval and subagent configuration.
- `agents/*.toml` — custom agents for Micronaut, architecture, review, DevOps, Hibernate, OpenAPI and testing.
- `rules/default.rules` — conservative command execution policy.
- `bin/*` — helper scripts for launching Codex and running verification.
- `prompts/*` — reusable task prompts.
- `snippets/*` — optional Gradle, CI and Java snippets.

## Recommended setup

```bash
chmod +x .codex/bin/*
.codex/bin/doctor
```

Start Codex normally from the repository root:

```bash
codex
```

Or use the project launcher:

```bash
.codex/bin/codex-project
```

The launcher intentionally does not set `CODEX_HOME`; this preserves your normal authentication and user-level configuration. Use `.codex/bin/codex-project-home` only if you intentionally want isolated repository-local Codex state.

## Verification

```bash
.codex/bin/quick-test
.codex/bin/verify
```

Test a command against local rules:

```bash
.codex/bin/check-rules -- kubectl get pods
```

## Project defaults

- Java: Oracle JDK 25 LTS.
- Model: `gpt-5.5`.
- Style: Google Java Style through Spotless / google-java-format.
- Business REST prefix: `/rest/v1`.
- OpenAPI JSON endpoint: `/doc`.
- Documentation UI endpoint: `/reference`.
- Default sandbox: `workspace-write` with network disabled.
- Approval policy: `on-request`.

## Notes

Keep personal defaults in `~/.codex/config.toml` and repository-specific behavior here. Keep `AGENTS.md` short, factual and focused on durable project rules.
