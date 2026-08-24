package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.api.dto.HealthLivenessResponse;
import com.example.javastarterboilerplate.api.dto.HealthReadinessResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Exposes the Kubernetes-standard liveness and readiness probe paths.
 *
 * <p>{@code GET /healthz} and {@code GET /readyz} delegate to {@link HealthController#live()} and
 * {@link HealthController#ready()} so the full health check, drain endpoint and probe logic share a
 * single implementation.
 */
@Tag(name = "Health")
@Controller
public class HealthProbeController {

  private final HealthController healthController;

  public HealthProbeController(HealthController healthController) {
    this.healthController = healthController;
  }

  @Get("/healthz")
  @Operation(
      summary = "Liveness probe",
      description = "Returns a basic liveness status used by orchestration systems.")
  public HealthLivenessResponse healthz() {
    return healthController.live();
  }

  @Get("/readyz")
  @Operation(
      summary = "Readiness probe",
      description = "Returns readiness status and dependency checks.")
  public HealthReadinessResponse readyz() {
    return healthController.ready();
  }
}
