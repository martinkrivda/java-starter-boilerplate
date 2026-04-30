---
description: Apply Spotless formatting and confirm the result with spotlessCheck.
allowed-tools: Bash(./gradlew spotlessApply*), Bash(./gradlew spotlessCheck*), Bash(git diff*), Bash(git status*)
---

Format the codebase to Google Java Style.

1. Run `./gradlew spotlessApply`.
2. Run `./gradlew spotlessCheck` to confirm clean state.
3. Run `git status` and `git diff --stat` to summarize what changed.

Do not commit. Do not stage. Just format and report.

If `spotlessCheck` still fails after `spotlessApply`, surface the exact violations — do not hand-edit to "fix" the formatter.
