{{/*
Chart name, honoring nameOverride.
*/}}
{{- define "java-starter-boilerplate.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Fully qualified app name, honoring fullnameOverride.
*/}}
{{- define "java-starter-boilerplate.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- if contains $name .Release.Name -}}
{{- .Release.Name | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- printf "%s-%s" .Release.Name $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Standard app.kubernetes.io/* labels.
*/}}
{{- define "java-starter-boilerplate.labels" -}}
helm.sh/chart: {{ printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" }}
{{ include "java-starter-boilerplate.selectorLabels" . }}
app.kubernetes.io/version: {{ .Chart.AppVersion | quote }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
{{- end -}}

{{/*
Selector labels.
*/}}
{{- define "java-starter-boilerplate.selectorLabels" -}}
app.kubernetes.io/name: {{ include "java-starter-boilerplate.name" . }}
app.kubernetes.io/instance: {{ .Release.Name }}
{{- end -}}

{{/*
Service account name, honoring serviceAccount.create / serviceAccount.name.
*/}}
{{- define "java-starter-boilerplate.serviceAccountName" -}}
{{- if .Values.serviceAccount.create -}}
{{- default (include "java-starter-boilerplate.fullname" .) .Values.serviceAccount.name -}}
{{- else -}}
{{- default "default" .Values.serviceAccount.name -}}
{{- end -}}
{{- end -}}

{{/*
Fully qualified image reference, honoring global.imageRegistry and appVersion fallback.
*/}}
{{- define "java-starter-boilerplate.image" -}}
{{- $tag := .Values.image.tag | default .Chart.AppVersion -}}
{{- if .Values.global.imageRegistry -}}
{{- printf "%s/%s:%s" .Values.global.imageRegistry .Values.image.repository $tag -}}
{{- else -}}
{{- printf "%s:%s" .Values.image.repository $tag -}}
{{- end -}}
{{- end -}}

{{/*
Name of the Secret referenced by envFrom, resolved with the priority:
externalSecret.targetName > existingSecret > "" (no secretRef).
*/}}
{{- define "java-starter-boilerplate.secretName" -}}
{{- if .Values.externalSecret.enabled -}}
{{- .Values.externalSecret.targetName -}}
{{- else -}}
{{- .Values.existingSecret -}}
{{- end -}}
{{- end -}}
