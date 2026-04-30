# Review Diff Prompt

```text
Review the current diff in this Java 25 LTS Micronaut repository.

Focus on:
- correctness and regression risk,
- package boundary violations,
- domain leakage of Micronaut/JPA/Hibernate/cloud SDK concerns,
- REST /rest/v1 prefix consistency,
- ApiResponse<T> response envelope consistency,
- RFC 9457-style error handling consistency,
- OpenAPI /doc and /reference behavior when touched,
- tests and coverage,
- security-sensitive changes,
- documentation updates.

Return:
- blocking issues first,
- non-blocking suggestions second,
- missing tests/docs third,
- a concise final verdict.
```
