package com.example.javastarterboilerplate.observability;

import static org.assertj.core.api.Assertions.assertThat;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import org.junit.jupiter.api.Test;

class RequestIdFilterTest {

  @Test
  void generatesAndPropagatesRequestIdWhenMissing() {
    RequestIdFilter filter = new RequestIdFilter();
    MutableHttpRequest<?> request = HttpRequest.GET("/test");
    var response = HttpResponse.ok();

    filter.onRequest(request);
    filter.onResponse(request, response);

    assertThat(request.getAttribute(RequestIdFilter.REQUEST_ID_ATTRIBUTE, String.class))
        .isPresent();
    assertThat(response.getHeaders().get(RequestIdFilter.HEADER_NAME)).isNotBlank();
    assertThat(response.getHeaders().get(RequestIdFilter.LEGACY_HEADER_NAME)).isNotBlank();
  }

  @Test
  void keepsProvidedRequestId() {
    RequestIdFilter filter = new RequestIdFilter();
    MutableHttpRequest<?> request =
        HttpRequest.GET("/test").header(RequestIdFilter.HEADER_NAME, "provided-id");
    var response = HttpResponse.ok();

    filter.onRequest(request);
    filter.onResponse(request, response);

    assertThat(response.getHeaders().get(RequestIdFilter.HEADER_NAME)).isEqualTo("provided-id");
    assertThat(response.getHeaders().get(RequestIdFilter.LEGACY_HEADER_NAME))
        .isEqualTo("provided-id");
  }

  @Test
  void acceptsLegacyCorrelationHeader() {
    RequestIdFilter filter = new RequestIdFilter();
    MutableHttpRequest<?> request =
        HttpRequest.GET("/test").header(RequestIdFilter.LEGACY_HEADER_NAME, "legacy-id");
    var response = HttpResponse.ok();

    filter.onRequest(request);
    filter.onResponse(request, response);

    assertThat(response.getHeaders().get(RequestIdFilter.HEADER_NAME)).isEqualTo("legacy-id");
  }
}
