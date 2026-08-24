# Deployment

## Docker

Build the image:

```bash
docker build -t java-starter-boilerplate:latest .
```

CI builds should pass traceability metadata into the image build:

```bash
docker build \
  --build-arg APP_VERSION=0.1.0 \
  --build-arg BUILD_NUMBER="${BUILD_NUMBER}" \
  --build-arg GIT_COMMIT="${GIT_COMMIT}" \
  --build-arg BUILD_TIMESTAMP="${BUILD_TIMESTAMP}" \
  -t java-starter-boilerplate:0.1.0 .
```

The runtime image uses:

- Eclipse Temurin JRE 25
- `curl` for local health checks and shutdown draining hooks
- non-root user
- Micronaut `prod` environment by default
- OCI labels for version, build number, source revision and build timestamp

## Docker Compose

The repository includes `compose.yaml` for local dependency startup (`docker compose` auto-discovers this filename; no `-f` flag needed).

Start PostgreSQL and MinIO:

```bash
docker compose up -d postgres minio
```

Start the application container as well:

```bash
docker compose up -d app
```

The `app` service includes a Compose health check against `/readyz` (scheme follows `TLS_ENABLED`) and uses a `30s` stop grace period so the process has time to drain on shutdown. Disabled optional integrations do not mark the container unready.

For source-level development on a host without Java, see [development.md](development.md).

## Kubernetes

Deployment manifests are packaged as a Helm chart at `deploy/helm/java-starter-boilerplate/`.

Render and validate locally before installing:

```bash
helm lint deploy/helm/java-starter-boilerplate
helm template my-release deploy/helm/java-starter-boilerplate \
  --set image.tag=0.1.0 \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=myapp.example.com
```

Install or upgrade:

```bash
helm upgrade --install my-release deploy/helm/java-starter-boilerplate \
  --namespace my-namespace \
  --set image.tag=0.1.0 \
  --set existingSecret=my-app-secret
```

Secrets (`DB_USER`, `DB_PASSWORD`, `STORAGE_S3_ACCESS_KEY`, `STORAGE_S3_SECRET_KEY`) are never
created by the chart directly. Provide them either as a pre-created `Secret` referenced via
`existingSecret`, or via HashiCorp Vault through the External Secrets Operator:

```bash
helm install my-release deploy/helm/java-starter-boilerplate \
  --set externalSecret.enabled=true \
  --set externalSecret.targetName=java-starter-secret \
  --set "externalSecret.data[0].secretKey=DB_PASSWORD" \
  --set "externalSecret.data[0].remoteRef.key=secret/myapp" \
  --set "externalSecret.data[0].remoteRef.property=db_password"
```

`values.schema.json` validates chart values in IDEs and on `helm lint`/`helm install`.
See `docs/superpowers/specs/2026-06-02-helm-chart-design.md` for the original chart design
(predates the TLS and probe-rename changes below).

Readiness and liveness probes point to the Kubernetes-standard paths:

- `/readyz` (readiness)
- `/healthz` (liveness)

The Deployment also configures graceful shutdown behavior:

- `terminationGracePeriodSeconds: 30`
- a `preStop` hook that calls `POST /health/drain` (scheme follows `tls.enabled`)
- readiness polling every `5s` so terminating pods leave service quickly before the JVM exits

Prometheus scrape endpoint:

- `/metrics`

The Service manifest includes scrape annotations for annotation-based Prometheus discovery.
If your platform runs Prometheus Operator, use a `ServiceMonitor` as the preferred integration.

### TLS

Native Micronaut TLS (see [configuration.md](configuration.md#tls)) is wired through the chart's
`tls.*` values, sourced from a cert-manager-issued `Secret` (`tls.crt` / `tls.key`):

```bash
helm upgrade --install my-release deploy/helm/java-starter-boilerplate \
  --set tls.enabled=true \
  --set tls.secretName=my-release-tls
```

Enabling `tls.enabled`:

- sets `TLS_ENABLED=true` (and `TLS_CERT_PATH` / `TLS_KEY_PATH`) on the container
- mounts `tls.secretName` read-only at `tls.mountPath` (default `/etc/tls`)
- switches `livenessProbe`/`readinessProbe` to `scheme: HTTPS`
- switches the `preStop` drain hook to HTTPS

The `Service` itself needs no change — it proxies TCP on `containerPort` regardless of which
scheme the backend speaks, since Micronaut 4.x cannot serve HTTP and HTTPS on separate ports in
one process (enabling TLS switches the whole app, including `/metrics`, to HTTPS).

For anything the built-in `tls.*` hook doesn't cover (a second secret, a ConfigMap, an
`emptyDir`), use `extraVolumes` / `extraVolumeMounts` without editing the chart templates:

```bash
helm upgrade --install my-release deploy/helm/java-starter-boilerplate \
  --set 'extraVolumes[0].name=extra-ca' \
  --set 'extraVolumes[0].secret.secretName=extra-ca-bundle' \
  --set 'extraVolumeMounts[0].name=extra-ca' \
  --set 'extraVolumeMounts[0].mountPath=/etc/ssl/extra-ca'
```
