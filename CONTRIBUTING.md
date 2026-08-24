# Contributing

## Workflow

- Use focused, reviewable changes.
- Follow Conventional Commits:
  - `feat:`
  - `fix:`
  - `chore:`
  - `docs:`
  - `refactor:`
  - `test:`
- Mark breaking changes with `!` in the type, for example `feat!:`, or with a `BREAKING CHANGE:` footer.
- Use Conventional Commits to guide SemVer bumps:
  - `fix:` maps to PATCH.
  - `feat:` maps to MINOR.
  - breaking changes map to MAJOR.
  - maintenance-only commits do not require a release bump unless they affect public behavior or published artifacts.
- Use lowercase branch prefixes:
  - `feature/`
  - `bugfix/`
  - `hotfix/`
  - `release/`

## Quality Bar

- Keep the project simple, explicit and easy to onboard into.
- Preserve the layered architecture.
- Do not introduce business workflows for sealing or signing in this starter phase.
- Add or update tests for every behavior change.
- Keep production coverage at 100% unless there is a documented bootstrap-only exclusion.
- Follow Google Java Style for Java sources.
- Run Spotless before opening a PR when touching formatted sources.
- Keep Spotless, Checkstyle, ArchUnit and Jacoco green in `./gradlew check`.
- Prefer IntelliJ IDEA for the smoothest Java 25, Micronaut and Gradle experience in this repository.

## Documentation Rule

Update `README.md`, `CONTRIBUTING.md`, `CHANGELOG.md`, or `docs/` when behavior, public API, infrastructure, or configuration changes.

## Pull Request Checklist

Every PR should summarize:

- scope
- impacted paths
- executed checks
- config changes
- infrastructure changes
- contract or public API changes
- version or changelog changes, if applicable
