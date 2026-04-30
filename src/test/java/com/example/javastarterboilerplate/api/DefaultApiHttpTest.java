package com.example.javastarterboilerplate.api;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.observability.RequestIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

@MicronautTest
@Property(name = "persistence.enabled", value = "false")
@Property(name = "persistence.vendor", value = "none")
@Property(name = "datasources.default.enabled", value = "false")
@Property(name = "flyway.datasources.default.enabled", value = "false")
class DefaultApiHttpTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Inject
  @Client("/")
  HttpClient httpClient;

  @Test
  void startsWithoutPersistenceAndServesInfoAndSampleEndpoints() {
    HttpResponse<String> infoResponse =
        httpClient.toBlocking().exchange(HttpRequest.GET("/rest/v1/info"), String.class);
    HttpResponse<String> listResponse =
        httpClient
            .toBlocking()
            .exchange(HttpRequest.GET("/rest/v1/sample-documents"), String.class);

    Map<String, Object> infoEnvelope = parseJson(infoResponse.body());
    Map<String, Object> listEnvelope = parseJson(listResponse.body());

    assertThat(infoResponse.getStatus().getCode()).isEqualTo(HttpStatus.OK.getCode());
    assertThat(data(infoEnvelope).get("activeDatabaseProfile")).isEqualTo("none");
    @SuppressWarnings("unchecked")
    List<String> activeEnvironments = (List<String>) data(infoEnvelope).get("activeEnvironments");
    assertThat(activeEnvironments).contains("test");
    assertThat(infoResponse.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();

    @SuppressWarnings("unchecked")
    List<Map<String, Object>> documents = (List<Map<String, Object>>) listEnvelope.get("data");
    assertThat(documents).isNotEmpty();
    assertThat(documents)
        .extracting(document -> document.get("name"))
        .contains("Seeded in-memory sample");
    assertThat(listResponse.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> data(Map<String, Object> envelope) {
    return (Map<String, Object>) envelope.get("data");
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
