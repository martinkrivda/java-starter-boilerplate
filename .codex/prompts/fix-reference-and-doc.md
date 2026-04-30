# Fix `/reference` and `/doc` Prompt

```text
Update this Java 25 LTS Micronaut repository to fix OpenAPI documentation behavior.

Current issue:
Opening /reference shows:

Document 'api-1' could not be loaded
openapi

Goals:
1. Keep /reference as the documentation UI endpoint.
2. Add or fix /doc so it returns the generated OpenAPI definition as JSON with content type application/json.
3. Ensure /reference loads the same OpenAPI document exposed by /doc.
4. Ensure public business REST paths in the OpenAPI document use /rest/v1.

Requirements:
- Search for reference, swagger, openapi, api-1, scalar, redoc and swagger-ui.
- Find where the UI config points to api-1.
- Verify what Micronaut actually generates.
- Reuse generated Micronaut OpenAPI output; do not duplicate OpenAPI generation manually.
- Add or update tests for /doc and /reference if the project has endpoint tests.
- Keep changes minimal.

Expected output:
- Root cause.
- Modified files.
- How /doc obtains the OpenAPI JSON.
- How /reference is configured to load it.
- Verification performed.
```
