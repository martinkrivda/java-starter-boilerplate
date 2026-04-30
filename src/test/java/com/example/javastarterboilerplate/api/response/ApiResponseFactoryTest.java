package com.example.javastarterboilerplate.api.response;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.javastarterboilerplate.config.ApiResponseProperties;
import com.example.javastarterboilerplate.observability.RequestIdFilter;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpRequest;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApiResponseFactoryTest {

  private final Clock clock = Clock.fixed(Instant.parse("2026-04-14T12:00:00Z"), ZoneOffset.UTC);

  @Test
  void createsSuccessResponses() {
    ApiResponseFactory factory = new ApiResponseFactory(clock, new ApiResponseProperties());
    MutableHttpRequest<?> request = request("request-1");

    ApiResponse<String> envelope = factory.success(request, "ok");
    var ok = factory.ok(request, "ok");
    var created = factory.created(request, "created", URI.create("/created"));

    assertThat(envelope.success()).isTrue();
    assertThat(envelope.data()).isEqualTo("ok");
    assertThat(envelope.error()).isNull();
    assertThat(envelope.meta().requestId()).isEqualTo("request-1");
    assertThat(envelope.meta().timestamp()).isEqualTo(Instant.parse("2026-04-14T12:00:00Z"));
    assertThat(ok.body()).isEqualTo(envelope);
    assertThat(created.getStatus().getCode()).isEqualTo(HttpStatus.CREATED.getCode());
    assertThat(created.getHeaders().get(HttpHeaders.LOCATION)).isEqualTo("/created");
  }

  @Test
  void createsErrorResponsesAndSupportsTrailingSlashProblemBaseUri() {
    ApiResponseProperties properties = new ApiResponseProperties();
    properties.setProblemBaseUri("https://api.example.com/problems/");
    ApiResponseFactory factory = new ApiResponseFactory(clock, properties);
    MutableHttpRequest<?> request = request("request-2");

    var response =
        factory.error(
            request,
            HttpStatus.BAD_REQUEST,
            "bad-request",
            "Bad request",
            "bad input",
            "E1001",
            List.of());

    assertThat(response.getStatus().getCode()).isEqualTo(HttpStatus.BAD_REQUEST.getCode());
    assertThat(response.getHeaders().get(HttpHeaders.CACHE_CONTROL)).isEqualTo("no-store");
    assertThat(response.body()).isNotNull();
    assertThat(response.body().success()).isFalse();
    assertThat(response.body().error().type())
        .isEqualTo("https://api.example.com/problems/bad-request");
    assertThat(response.body().error().errors()).isNull();
  }

  private MutableHttpRequest<?> request(String requestId) {
    MutableHttpRequest<?> request = HttpRequest.GET("/api/test");
    request.setAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, requestId);
    return request;
  }
}
