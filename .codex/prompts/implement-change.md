# Implement Change Prompt

Use this prompt for normal implementation tasks.

```text
You are working in a Java 25 LTS Micronaut microservice repository.

Task:
<describe the requested change>

Constraints:
- Follow AGENTS.md.
- Use Google Java Style through Spotless.
- Public REST API endpoints must use /rest/v1.
- /doc must return OpenAPI JSON and /reference must remain the documentation UI when relevant.
- Keep controllers thin and return ApiResponse<T>.
- Keep domain free of Micronaut, JPA, Hibernate and cloud SDK dependencies.
- Minimize changes.

Expected workflow:
1. Inspect the existing implementation and package boundaries.
2. Propose a short plan when the change is non-trivial.
3. Implement the minimal change.
4. Add or update relevant tests.
5. Update docs if behavior, API, configuration or infrastructure changed.
6. Run relevant verification commands.
7. Summarize modified files, verification performed, assumptions and risks.
```
