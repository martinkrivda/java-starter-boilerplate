---
description: Inspect current Kubernetes context and namespace before any cluster-related discussion. Read-only.
allowed-tools: Bash(kubectl config current-context), Bash(kubectl config view --minify*), Bash(kubectl config get-contexts*)
---

Show the active Kubernetes context, namespace and cluster server URL so the user can confirm safety before any cluster-changing action is even discussed.

1. `kubectl config current-context`
2. `kubectl config view --minify --output 'jsonpath={..namespace}'`
3. `kubectl config view --minify --output 'jsonpath={.clusters[0].cluster.server}'`

Report the values plainly. Do not run any other `kubectl` command. Do not suggest cluster-changing actions in the same response — that is a separate, explicit step requiring user approval per `CLAUDE.md` section 12.

If the context name or server URL looks like production (e.g. contains `prod`, `production`, `live`), call it out clearly and remind the user that `kubectl apply | delete | patch | scale | rollout` are blocked by default.
