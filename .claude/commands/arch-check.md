---
description: Run architecture/package-boundary tests (ArchUnit) and summarize layering violations.
allowed-tools: Bash(./gradlew test*), Read(./**), Grep(./**), Glob(./**)
---

Verify layered architecture rules defined in `CLAUDE.md` section 4.

1. Locate ArchUnit test classes (typically under `src/test/java/**/architecture/**` or `*ArchitectureTest*`).
   - If none exist, report this as a gap, do not invent tests.
2. Run only those tests:
   `./gradlew test --tests "*Architecture*" --tests "*ArchUnit*"`
3. Report:
   - Which rules were checked.
   - Which violations (if any) were found.
   - Files that violate package boundaries:
     - `domain` importing Micronaut / JPA / vendor SDKs
     - `api` calling `infrastructure` directly
     - `application` importing framework annotations from `infrastructure`

Do not "fix" violations automatically. Report them with file paths and offending imports, then stop and ask.
