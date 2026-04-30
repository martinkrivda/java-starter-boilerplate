# `.claude/` — Claude Code workspace configuration

This directory configures [Claude Code](https://docs.claude.com/en/docs/claude-code) for this Micronaut starter. It is a peer to `.codex/` (do not remove `.codex/` files).

## Files

```
.claude/
├── README.md                         # this file
├── settings.json                     # permissions, sandbox, deny rules
├── commands/                         # custom slash commands
│   ├── verify.md                     # /verify       — full local CI parity
│   ├── format.md                     # /format       — Spotless apply + check
│   ├── arch-check.md                 # /arch-check   — ArchUnit boundary tests
│   ├── openapi-check.md              # /openapi-check — OpenAPI / /doc / /reference
│   ├── k8s-context.md                # /k8s-context  — read-only context inspection
│   └── scope-check.md                # /scope-check  — starter-scope guard
└── agents/                           # subagents (delegated tasks)
    ├── architecture-reviewer.md      # layering / scope / speculative abstractions
    ├── test-author.md                # writes deterministic tests
    └── security-gatekeeper.md        # blocks secret/destructive/external actions
```

`CLAUDE.md` lives in the repository root and is loaded automatically by Claude Code.

## Schema compatibility

> **Important.** The exact permission and sandbox keys in `settings.json` must be verified against the installed Claude Code version. The schema evolves and unrecognized keys are silently ignored.
>
> Check:
>
> ```bash
> claude --version
> claude config list
> ```
>
> Reference: <https://docs.claude.com/en/docs/claude-code/settings>
>
> If a key is unsupported in your version, fall back to the documented equivalents. The intent of every rule in `settings.json` is encoded as plain prose in `CLAUDE.md` section 16, which Claude reads on every session — so even if the machine-enforced rule is missing, the documented rule still applies.

## How to use

- Slash commands: type `/verify`, `/format`, `/arch-check`, etc. in Claude Code.
- Subagents: invoke explicitly, for example `Use the architecture-reviewer agent to review the diff`. Claude may also call them automatically when their description matches.
- Permissions: anything not in `allow` falls into `ask`. Anything in `deny` is refused outright.

## Editing the config

When changing `settings.json` or any agent / command file:

1. Run `claude config list` to confirm the file is parsed.
2. Test in a throwaway change first.
3. Update this README if you add or remove commands / agents.
4. Keep parity with `.codex/` where the underlying intent is the same.

## Relationship to `CLAUDE.md`

`CLAUDE.md` is the source of truth for project rules. `settings.json` is the machine-enforced subset. If they ever disagree, `CLAUDE.md` wins and `settings.json` should be brought in line.
