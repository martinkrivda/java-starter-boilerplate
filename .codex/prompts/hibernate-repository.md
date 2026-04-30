# Hibernate / Repository Prompt

```text
Implement or review a Hibernate/JPA persistence change.

Rules:
- Persistence belongs in infrastructure.
- Domain must not depend on JPA/Hibernate annotations or entities unless explicitly accepted as a project decision.
- Do not expose entities from REST DTOs.
- Keep transaction boundaries explicit and close to application use cases.
- Keep PostgreSQL and SQL Server portability in mind.
- Add repository contract tests and migration tests where relevant.
- Use Testcontainers for database integration tests when available.
```
