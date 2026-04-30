---
description: Run the full local verification chain (mirrors CI) and report exactly what passed.
allowed-tools: Bash(make check*), Bash(make test*), Bash(make format*), Bash(./gradlew*), Read(./**)
---

Run the verification chain that mirrors CI, then report results honestly.

Preferred path (single command):

```
make check
```

This runs tests, the 100% coverage gate, and `spotlessCheck` together — see `CLAUDE.md` section 3.

If `make` is unavailable or the user wants step-by-step output, fall back to Gradle:

1. `./gradlew spotlessCheck`
2. `./gradlew test`
3. `./gradlew jacocoTestReport`
4. `./gradlew jacocoTestCoverageVerification`
5. `./gradlew check`

Stop on the first failure.

Report rules:

- State which steps actually ran and which were skipped due to a prior failure.
- For failures, include the relevant error excerpt (do not paste the whole log).
- For coverage failures, include the failing class and line counts — the gate is **100%** (only `Application.java` excluded).
- Do not claim a step passed unless it actually ran and exited 0.
- If a step cannot run (missing tool, sandbox, no Java 25.0.2), say so explicitly and suggest `make docker-check` as an alternative.
- End with a one-line summary: `PASS` or `FAIL: <step>`.
