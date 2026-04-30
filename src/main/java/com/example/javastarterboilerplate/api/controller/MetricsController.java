package com.example.javastarterboilerplate.api.controller;

import io.micronaut.configuration.metrics.micrometer.prometheus.management.PrometheusEndpoint;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;

@Tag(name = "Monitoring")
@Controller("/metrics")
public class MetricsController {

  private static final MediaType PROMETHEUS_MEDIA_TYPE =
      MediaType.of("text/plain; version=0.0.4; charset=utf-8");

  private final PrometheusEndpoint prometheusEndpoint;

  @Inject
  public MetricsController(PrometheusEndpoint prometheusEndpoint) {
    this.prometheusEndpoint = prometheusEndpoint;
  }

  @Get(produces = "text/plain; version=0.0.4; charset=utf-8")
  @Operation(
      summary = "Prometheus metrics scrape endpoint",
      description = "Exposes runtime and application metrics in Prometheus text format.")
  public HttpResponse<String> scrape() {
    return HttpResponse.ok(prometheusEndpoint.scrape())
        .contentType(PROMETHEUS_MEDIA_TYPE)
        .header(HttpHeaders.CACHE_CONTROL, "no-store");
  }
}
