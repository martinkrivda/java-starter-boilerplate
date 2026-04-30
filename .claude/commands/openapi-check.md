---
description: Verify OpenAPI generation and the /doc, /doc/openapi.json, /doc/openapi.yaml endpoints.
allowed-tools: Bash(./gradlew*), Bash(make test*), Read(./**), Grep(./**), Glob(./**)
---

Check the OpenAPI / docs setup defined in `CLAUDE.md` sections 5 and 6.

1. Locate the generated OpenAPI artifact (Micronaut OpenAPI typically writes to `build/classes/java/main/META-INF/swagger/*.yml` or under `build/generated/`).
2. Confirm:
   - `GET /doc` serves the **Scalar UI**.
   - `GET /doc/openapi.json` returns the generated OpenAPI document with `Content-Type: application/json`.
   - `GET /doc/openapi.yaml` returns the same document as YAML.
   - The Scalar UI references the same generated artifact (no filename mismatch).
   - `/health/**` and `/metrics` are not wrapped in `ApiResponse<T>`; everything else is.
3. Run the relevant tests:
   `./gradlew test --tests "*OpenApi*" --tests "*Doc*" --tests "*Scalar*"`

Report findings with concrete file paths and excerpts. Do not modify configuration without confirmation.
