# Deployment

## Docker

Build the image:

```bash
docker build -t java-starter-boilerplate:latest .
```

The runtime image uses:

- Eclipse Temurin JRE 25
- `curl` for local health checks and shutdown draining hooks
- non-root user
- Micronaut `prod` environment by default

## Docker Compose

The repository includes `docker-compose.yaml` for local dependency startup.

Start PostgreSQL and MinIO:

```bash
docker compose up -d postgres minio
```

Start the application container as well:

```bash
docker compose up -d app
```

The `app` service includes a Compose health check against `http://127.0.0.1:8080/health/ready` and uses a `30s` stop grace period so the process has time to drain on shutdown. Disabled optional integrations do not mark the container unready.

For source-level development on a host without Java, see [development.md](development.md).

## Kubernetes

Manifests live in `k8s/`:

- `configmap.yaml`
- `secret.yaml`
- `service.yaml`
- `deployment.yaml`

Apply them with:

```bash
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/deployment.yaml
```

Readiness and liveness probes point to:

- `/health/ready`
- `/health/live`

The Deployment also configures graceful shutdown behavior:

- `terminationGracePeriodSeconds: 30`
- a `preStop` hook that calls `POST /health/drain`
- readiness polling every `5s` so terminating pods leave service quickly before the JVM exits

Prometheus scrape endpoint:

- `/metrics`

The Service manifest includes scrape annotations for annotation-based Prometheus discovery.
If your platform runs Prometheus Operator, use a `ServiceMonitor` as the preferred integration.
