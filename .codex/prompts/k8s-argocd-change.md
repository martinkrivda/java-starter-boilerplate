# Kubernetes / ArgoCD Change Prompt

```text
Review or implement a Kubernetes/ArgoCD change for this Micronaut microservice.

Safety rules:
- Prefer read-only inspection and manifest diffs.
- Do not run kubectl apply/delete/patch, helm upgrade, argocd app sync, argocd app delete or production-changing commands without explicit approval.
- Do not touch secrets, kubeconfigs, production namespaces or cluster credentials.
- Keep manifests environment-aware and GitOps-friendly.

Expected checks:
- deployment image/tag/version source,
- probes and ports,
- config maps/secrets references,
- resource requests/limits,
- service/account/RBAC scope,
- ArgoCD sync policy and namespace safety,
- GitHub/GitLab CI consistency.
```
