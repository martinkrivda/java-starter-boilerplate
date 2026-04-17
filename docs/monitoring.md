# Monitoring

## Prometheus Endpoint

The service exposes Prometheus metrics at:

- `GET /metrics`

Response format:

- `Content-Type: text/plain; version=0.0.4; charset=utf-8`
- Prometheus scrape format (`# HELP`, `# TYPE`, samples)

This endpoint is intentionally outside the JSON response envelope because Prometheus requires plain text exposition format.

## Configuration

Prometheus export is enabled by default.

Relevant keys:

- `micronaut.metrics.enabled`
- `micronaut.metrics.export.prometheus.enabled`
- `endpoints.prometheus.enabled`
- `endpoints.prometheus.sensitive`

To disable metric export:

```bash
METRICS_PROMETHEUS_ENABLED=false
```

## Prometheus Stack Best Practices

- Scrape via Kubernetes Service, not Pod IPs.
- Keep metrics endpoint unauthenticated only inside trusted network boundaries.
- Prefer stable, low-cardinality labels; avoid user IDs, document IDs, request IDs, or arbitrary payload fragments in metric labels.
- Use recording rules and alerts in Prometheus for SLO-relevant signals (latency, error rate, availability).
- Keep scrape intervals consistent across environments (for example 15s or 30s) and align alert windows with that interval.
- Set retention and remote-write strategy centrally in Prometheus, not in the application.

## Kubernetes Notes

`k8s/service.yaml` includes standard scrape annotations:

- `prometheus.io/scrape: "true"`
- `prometheus.io/path: /metrics`
- `prometheus.io/port: "8080"`

If your cluster uses Prometheus Operator, prefer a `ServiceMonitor` resource and keep annotation-based scraping only as fallback.
