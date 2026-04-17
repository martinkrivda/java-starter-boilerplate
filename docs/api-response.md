# API Response Standard

This starter implements a shared JSON response envelope for service endpoints that return `application/json`.

Success shape:

```json
{
  "success": true,
  "data": {},
  "error": null,
  "meta": {
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2026-02-04T10:30:00Z"
  }
}
```

Error shape:

```json
{
  "success": false,
  "data": null,
  "error": {
    "type": "https://api.example.com/problems/validation-error",
    "title": "Validation error",
    "status": 422,
    "detail": "Request is not valid.",
    "instance": "/rest/v1/sample-documents",
    "code": "E1003",
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "errors": [
      {
        "pointer": "/name",
        "field": "name",
        "code": "REQUIRED",
        "message": "must not be blank"
      }
    ]
  },
  "meta": {
    "requestId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2026-02-04T10:30:00Z"
  }
}
```

Implementation notes:

- `meta.requestId` is always present.
- `error.requestId` duplicates the same value for easier client parsing.
- `meta.timestamp` is emitted from `Instant` and serialized in RFC 3339-compatible `date-time` format.
- Validation errors use `422 Unprocessable Entity` in Micronaut terms and include `errors[]`.
- Error responses set `Cache-Control: no-store`.
- Internal errors return sanitized detail text without stack traces or infrastructure leakage.

Intentional exceptions:

- Binary or streamed success responses may stay outside the envelope.
- `/doc/openapi.json` and `/doc/openapi.yaml` stay raw so they remain valid OpenAPI artifacts.
- `/metrics` stays raw (`text/plain`) because Prometheus scraping requires text exposition format.

Configured defaults:

- JSON API route prefix: `app.api.prefix` defaulting to `/rest/v1`
- problem base URI: `api.response.problem-base-uri`
- canonical response header: `X-Request-Id`
- compatibility alias: `X-Correlation-Id`
