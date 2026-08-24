# Helm Chart Design

**Date:** 2026-06-02
**Status:** Approved
**Approach:** Production-grade chart with values.schema.json, ExternalSecret, Ingress, ServiceAccount

## Problem

The project currently ships raw Kubernetes manifests in `k8s/` (ConfigMap, Secret, Service,
Deployment). These cannot be customised without editing YAML directly, have no validation,
and cannot integrate with External Secrets Operator or modern GitOps tooling (ArgoCD). A Helm
chart provides parameterised, validated, reusable deployment manifests that follow the same
quality bar as the reference chart used in related services.

## Goals

- Add a production-grade Helm chart at `deploy/helm/java-starter-boilerplate/`
- Replace `k8s/` plain manifests (directory deleted)
- Support ExternalSecret (ESO / Vault) for secret management
- Support Ingress with TLS
- Include `values.schema.json` for IDE validation
- Follow the same structure and conventions as the reference pep-signing-portal chart

## Out of Scope

- Subcharts for PostgreSQL, MinIO, or other dependencies
- Helm test hooks (`templates/tests/`)
- Chart publishing to a Helm repository

## Chart Location

```
deploy/helm/java-starter-boilerplate/
```

## File Structure

```
deploy/helm/java-starter-boilerplate/
├── Chart.yaml
├── values.yaml
├── values.schema.json
└── templates/
    ├── _helpers.tpl
    ├── deployment.yaml
    ├── service.yaml
    ├── serviceaccount.yaml
    ├── configmap.yaml
    ├── ingress.yaml
    ├── externalsecret.yaml
    └── NOTES.txt
```

## Chart.yaml

```yaml
apiVersion: v2
name: java-starter-boilerplate
description: Micronaut starter for document sealing and signing services
type: application
version: 0.1.0
appVersion: "0.1.0-SNAPSHOT"
```

## values.yaml

```yaml
# yaml-language-server: $schema=values.schema.json

global:
  imageRegistry: ""
  imagePullSecrets: []

nameOverride: ""
fullnameOverride: ""

serviceAccount:
  create: true
  annotations: {}
  name: ""

podAnnotations: {}
podLabels: {}

podSecurityContext:
  runAsNonRoot: true
  seccompProfile:
    type: RuntimeDefault

securityContext:
  allowPrivilegeEscalation: false
  capabilities:
    drop: [ALL]

replicaCount: 2

image:
  repository: java-starter-boilerplate
  tag: ""
  pullPolicy: IfNotPresent

containerPort: 8080

env:
  MICRONAUT_ENVIRONMENTS: "prod,postgresql"
  APP_NAME: "java-starter-boilerplate"
  LOG_TARGET: "stdout"
  LOG_FORMAT: "json"
  LOG_LEVEL: "INFO"
  DB_HOST: "postgres"
  DB_PORT: "5432"
  DB_NAME: "starter"
  DB_POOL_MAX_SIZE: "10"
  STORAGE_S3_ENABLED: "true"
  STORAGE_S3_ENDPOINT: "http://minio:9000"
  STORAGE_S3_REGION: "eu-central-1"
  STORAGE_S3_BUCKET: "starter-documents"
  STORAGE_S3_PATH_STYLE_ACCESS: "true"
  PDFBOX_ENABLED: "true"
  DSS_ENABLED: "true"

existingSecret: ""

service:
  type: ClusterIP
  port: 80

resources:
  requests:
    cpu: 250m
    memory: 512Mi
  limits:
    cpu: "1"
    memory: 1Gi

livenessProbe:
  httpGet:
    path: /health/live
    port: http
  initialDelaySeconds: 20
  periodSeconds: 20
  timeoutSeconds: 3
  failureThreshold: 3

readinessProbe:
  httpGet:
    path: /health/ready
    port: http
  initialDelaySeconds: 10
  periodSeconds: 5
  timeoutSeconds: 3
  failureThreshold: 3

ingress:
  enabled: false
  className: ""
  annotations: {}
  hosts:
    - host: java-starter-boilerplate.example.com
      paths:
        - path: /
          pathType: Prefix
  tls: []

externalSecret:
  enabled: false
  apiVersion: external-secrets.io/v1
  refreshInterval: 1h
  secretStoreRef:
    name: vault
    kind: ClusterSecretStore
  targetName: ""
  data: []

nodeSelector: {}
tolerations: []
affinity: {}
```

## Template Details

### `_helpers.tpl`

Defines:
- `java-starter-boilerplate.name` — chart name with nameOverride
- `java-starter-boilerplate.fullname` — release + chart name with fullnameOverride
- `java-starter-boilerplate.labels` — standard Helm labels (app.kubernetes.io/*)
- `java-starter-boilerplate.selectorLabels` — matchLabels subset
- `java-starter-boilerplate.serviceAccountName` — resolves create/name logic
- `java-starter-boilerplate.image` — resolves `global.imageRegistry` + `image.repository` + tag

### `deployment.yaml`

- `replicas: {{ .Values.replicaCount }}`
- `terminationGracePeriodSeconds: 30`
- `preStop` hook: `curl -fsS -X POST http://127.0.0.1:8080/health/drain || true; sleep 10`
- `envFrom`:
  - `configMapRef` → ConfigMap from `env` values
  - `secretRef` → Secret name resolved from `existingSecret` or `externalSecret.targetName`
    (if both empty, the secretRef is omitted)
- `securityContext` and `podSecurityContext` from values
- `livenessProbe` and `readinessProbe` from values
- Prometheus scrape annotations on Pod template:
  `prometheus.io/scrape: "true"`, `prometheus.io/path: /metrics`, `prometheus.io/port: "8080"`
- `image`: `{{ include "java-starter-boilerplate.image" . }}`

### `service.yaml`

- `type: ClusterIP`, `port: 80 → targetPort: 8080`
- Prometheus scrape annotations on Service (for annotation-based discovery)

### `configmap.yaml`

- Iterates `.Values.env` to produce a ConfigMap with all non-secret environment variables

### `serviceaccount.yaml`

- Conditional on `serviceAccount.create: true`
- Supports custom annotations (for IAM role binding, Workload Identity, etc.)

### `ingress.yaml`

- Conditional on `ingress.enabled: true`
- Supports multiple hosts, paths, pathType, and TLS
- `ingressClassName` from `ingress.className`

### `externalsecret.yaml`

- Conditional on `externalSecret.enabled: true`
- Generates an `ExternalSecret` resource (ESO)
- `targetName` — name of the resulting K8s Secret (referenced by Deployment's secretRef)
- `data[]` — list of `{secretKey, remoteRef: {key, property}}` mappings

### Secret resolution priority in Deployment

1. `externalSecret.enabled: true` → use `externalSecret.targetName` as secretRef
2. `existingSecret != ""` → use `existingSecret` as secretRef
3. Both empty → no secretRef (Deployment has no secret env vars)

### `NOTES.txt`

Post-install output:
- Application URL (Ingress host if enabled, or `kubectl port-forward` command)
- How to verify: `kubectl get pods -l app.kubernetes.io/name=java-starter-boilerplate`
- Link to docs/deployment.md

### `values.schema.json`

JSON Schema covering all top-level keys with:
- Types (`string`, `integer`, `boolean`, `object`, `array`)
- Required fields (`image.repository`)
- Enum constraints where applicable (`image.pullPolicy`, `service.type`)
- `additionalProperties: false` on leaf objects to catch typos early

## Deleted Files

The following are removed as part of this change:

- `k8s/configmap.yaml`
- `k8s/deployment.yaml`
- `k8s/service.yaml`
- `k8s/secret.yaml`
- `k8s/` directory

## Updated Files

- `docs/deployment.md` — Kubernetes section replaced with Helm usage instructions

## Testing Strategy

No production Java class changes. Verification:

1. **`helm lint deploy/helm/java-starter-boilerplate`** — validates chart syntax and schema
2. **`helm template deploy/helm/java-starter-boilerplate --dry-run`** — renders all templates
   and confirms no missing values or template errors
3. **`helm template ... | kubectl apply --dry-run=client -f -`** — validates rendered manifests
   against Kubernetes API schema (read-only, no cluster changes)
4. **`make check`** — full Java build + tests confirm no production code was broken

## Example Usage

```bash
# Dry-run render
helm template my-release deploy/helm/java-starter-boilerplate \
  --set image.tag=0.1.0 \
  --set ingress.enabled=true \
  --set ingress.hosts[0].host=myapp.example.com

# Install
helm install my-release deploy/helm/java-starter-boilerplate \
  --namespace my-namespace \
  --set image.tag=0.1.0 \
  --set existingSecret=my-app-secret

# With ExternalSecret
helm install my-release deploy/helm/java-starter-boilerplate \
  --set externalSecret.enabled=true \
  --set externalSecret.targetName=java-starter-secret \
  --set "externalSecret.data[0].secretKey=DB_PASSWORD" \
  --set "externalSecret.data[0].remoteRef.key=secret/myapp" \
  --set "externalSecret.data[0].remoteRef.property=db_password"
```
