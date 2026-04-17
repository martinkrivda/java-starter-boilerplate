package com.example.javastarterboilerplate.api;

import com.example.javastarterboilerplate.observability.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MicronautTest(environments = "test")
class ApiHttpTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();

    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void exposesFullHealthEndpoint() {
        HttpResponse<String> response = httpClient.toBlocking().exchange(HttpRequest.GET("/health"), String.class);
        Map<String, Object> health = parseJson(response.body());

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());
        assertThat(health.get("status")).isEqualTo("UP");
        assertThat(health).containsKey("version");
        assertThat(health).containsKey("uptime");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) health.get("checks");
        assertThat(checks).isNotEmpty();
        assertThat(checks.get(0)).containsKeys("name", "status", "responseTimeMs", "message", "details");
        assertThat(response.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();
    }

    @Test
    void exposesReadinessAndLivenessEndpoints() {
        HttpResponse<String> readinessResponse = httpClient.toBlocking().exchange(HttpRequest.GET("/health/ready"),
                String.class);
        HttpResponse<String> livenessResponse = httpClient.toBlocking().exchange(HttpRequest.GET("/health/live"),
                String.class);
        Map<String, Object> readiness = parseJson(readinessResponse.body());
        Map<String, Object> liveness = parseJson(livenessResponse.body());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> checks = (List<Map<String, Object>>) readiness.get("checks");
        assertThat(checks).isNotEmpty();
        boolean hasBlockingFailure = checks.stream().anyMatch(check -> {
            if (!"DOWN".equals(check.get("status"))) {
                return false;
            }
            Object details = check.get("details");
            if (!(details instanceof Map<?, ?> detailMap)) {
                return true;
            }
            return !Boolean.FALSE.equals(detailMap.get("enabled"));
        });
        assertThat(readiness.get("status")).isEqualTo(hasBlockingFailure ? "not_ready" : "ready");
        assertThat(checks.get(0)).containsKeys("name", "status", "responseTimeMs", "message", "details");
        assertThat(liveness.get("status")).isEqualTo("UP");
        assertThat(readinessResponse.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();
        assertThat(livenessResponse.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();
    }

    @Test
    void exposesInfoEndpointInEnvelope() {
        HttpResponse<String> response = httpClient.toBlocking().exchange(HttpRequest.GET("/rest/v1/info"),
                String.class);
        Map<String, Object> envelope = parseJson(response.body());

        assertThat(data(envelope).get("name")).isEqualTo("java-starter-boilerplate");
        assertThat(data(envelope).get("activeDatabaseProfile")).isEqualTo("h2");
        assertThat(((List<?>) data(envelope).get("integrations"))).hasSize(3);
        assertMeta(envelope, response.getHeaders().get(RequestIdFilter.HEADER_NAME));
    }

    @Test
    void createsAndReadsSampleDocumentsInEnvelope() {
        HttpRequest<Map<String, Object>> createRequest = HttpRequest.POST("/rest/v1/sample-documents",
                Map.of("name", "API sample", "storageKey", "incoming/api-sample.pdf"));

        HttpResponse<String> createResponse = httpClient.toBlocking().exchange(createRequest, String.class);
        Map<String, Object> createdEnvelope = parseJson(createResponse.body());
        String createdId = data(createdEnvelope).get("id").toString();

        HttpResponse<String> getResponse = httpClient.toBlocking()
                .exchange(HttpRequest.GET("/rest/v1/sample-documents/" + createdId), String.class);
        HttpResponse<String> listResponse = httpClient.toBlocking()
                .exchange(HttpRequest.GET("/rest/v1/sample-documents"), String.class);
        Map<String, Object> reloadedEnvelope = parseJson(getResponse.body());
        Map<String, Object> listEnvelope = parseJson(listResponse.body());

        assertThat(createResponse.getStatus().getCode()).isEqualTo(HttpStatus.CREATED.getCode());
        assertThat(data(createdEnvelope).get("name")).isEqualTo("API sample");
        assertThat(createResponse.getHeaders().get(HttpHeaders.LOCATION)).endsWith(createdId);
        assertThat(data(reloadedEnvelope).get("id")).isEqualTo(createdId);
        assertThat(((List<?>) listEnvelope.get("data"))).isNotEmpty();
        assertMeta(createdEnvelope, createResponse.getHeaders().get(RequestIdFilter.HEADER_NAME));
    }

    @Test
    void returnsProblemEnvelopeForUnknownDocument() {
        UUID missingId = UUID.randomUUID();

        assertThatThrownBy(() -> httpClient.toBlocking()
                .retrieve(HttpRequest.GET("/rest/v1/sample-documents/" + missingId), String.class))
                .isInstanceOf(HttpClientResponseException.class).satisfies(exception -> {
                    HttpClientResponseException clientException = (HttpClientResponseException) exception;
                    Map<String, Object> envelope = parseJson(
                            clientException.getResponse().getBody(String.class).orElseThrow());

                    assertThat(clientException.getStatus().getCode()).isEqualTo(HttpStatus.NOT_FOUND.getCode());
                    assertThat(envelope.get("success")).isEqualTo(false);
                    assertThat(envelope.get("data")).isNull();
                    assertThat(error(envelope).get("status")).isEqualTo(404);
                    assertThat(error(envelope).get("code")).isEqualTo("E1004");
                    assertThat(error(envelope).get("type"))
                            .isEqualTo("https://api.example.com/problems/resource-not-found");
                    assertThat(error(envelope).get("requestId")).isEqualTo(meta(envelope).get("requestId"));
                });
    }

    @Test
    void returnsValidationProblemEnvelope() {
        HttpRequest<Map<String, Object>> createRequest = HttpRequest.POST("/rest/v1/sample-documents",
                Map.of("name", "", "storageKey", ""));

        assertThatThrownBy(() -> httpClient.toBlocking().retrieve(createRequest, String.class))
                .isInstanceOf(HttpClientResponseException.class).satisfies(exception -> {
                    HttpClientResponseException clientException = (HttpClientResponseException) exception;
                    Map<String, Object> envelope = parseJson(
                            clientException.getResponse().getBody(String.class).orElseThrow());

                    assertThat(clientException.getStatus().getCode())
                            .isEqualTo(HttpStatus.UNPROCESSABLE_ENTITY.getCode());
                    assertThat(error(envelope).get("code")).isEqualTo("E1003");
                    assertThat(error(envelope).get("status")).isEqualTo(422);
                    assertThat(error(envelope).get("type"))
                            .isEqualTo("https://api.example.com/problems/validation-error");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> errors = (List<Map<String, Object>>) error(envelope).get("errors");
                    assertThat(errors).hasSize(2);
                    assertThat(errors).extracting(item -> item.get("pointer")).contains("/name", "/storageKey");
                    assertThat(errors).extracting(item -> item.get("code")).containsOnly("REQUIRED");
                });
    }

    @Test
    void propagatesCanonicalRequestIdHeader() {
        HttpResponse<String> response = httpClient.toBlocking().exchange(
                HttpRequest.GET("/health/live").header(RequestIdFilter.HEADER_NAME, "test-request-id"), String.class);
        Map<String, Object> body = parseJson(response.body());

        assertThat(response.getHeaders().get(RequestIdFilter.HEADER_NAME)).isEqualTo("test-request-id");
        assertThat(response.getHeaders().get(RequestIdFilter.LEGACY_HEADER_NAME)).isEqualTo("test-request-id");
        assertThat(body.get("status")).isEqualTo("UP");
    }

    @Test
    void exposesReferenceUiAndRawOpenApiDocuments() throws Exception {
        String html = httpClient.toBlocking().retrieve(HttpRequest.GET("/doc"), String.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> json = objectMapper.readValue(
                httpClient.toBlocking().retrieve(HttpRequest.GET("/doc/openapi.json"), String.class), Map.class);
        @SuppressWarnings("unchecked")
        Map<String, Object> yaml = yamlMapper.readValue(
                httpClient.toBlocking().retrieve(HttpRequest.GET("/doc/openapi.yaml"), String.class), Map.class);

        assertThat(html).contains("Scalar.createApiReference");
        assertThat(html).contains("openapi.json");
        assertThat(json.get("openapi")).isEqualTo("3.1.1");
        assertThat(json).containsKey("paths");
        @SuppressWarnings("unchecked")
        Map<String, Object> paths = (Map<String, Object>) json.get("paths");
        assertThat(paths).containsKey("/metrics");
        assertThat(yaml.get("openapi")).isEqualTo("3.1.1");
        assertThat(yaml).containsKey("paths");
    }

    @Test
    void exposesLegacyReferenceRouteAndSupportsDownloads() {
        String legacyReference = httpClient.toBlocking().retrieve(HttpRequest.GET("/reference"), String.class);
        HttpResponse<String> yamlDownload = httpClient.toBlocking()
                .exchange(HttpRequest.GET("/doc/openapi.yaml/download"), String.class);
        HttpResponse<String> jsonDownload = httpClient.toBlocking()
                .exchange(HttpRequest.GET("/doc/openapi.json/download"), String.class);

        assertThat(legacyReference).contains("Scalar.createApiReference");
        assertThat(yamlDownload.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"openapi.yaml\"");
        assertThat(jsonDownload.getHeaders().get(HttpHeaders.CONTENT_DISPOSITION))
                .isEqualTo("attachment; filename=\"openapi.json\"");
    }

    @Test
    void exposesPrometheusMetricsEndpoint() {
        HttpResponse<String> response = httpClient.toBlocking().exchange(HttpRequest.GET("/metrics"), String.class);

        assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());
        assertThat(response.getHeaders().get(HttpHeaders.CONTENT_TYPE)).startsWith("text/plain; version=0.0.4");
        assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
        assertThat(response.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();
        assertThat(response.body()).contains("# HELP");
        assertThat(response.body()).contains("# TYPE");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> data(Map<String, Object> envelope) {
        return (Map<String, Object>) envelope.get("data");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> error(Map<String, Object> envelope) {
        return (Map<String, Object>) envelope.get("error");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> meta(Map<String, Object> envelope) {
        return (Map<String, Object>) envelope.get("meta");
    }

    private void assertMeta(Map<String, Object> envelope, String requestId) {
        assertThat(requestId).isNotBlank();
        assertThat(meta(envelope).get("requestId")).isEqualTo(requestId);
        assertThat(meta(envelope).get("timestamp").toString()).contains("T");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseJson(String body) {
        try {
            return objectMapper.readValue(body, Map.class);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to parse JSON body", exception);
        }
    }
}
