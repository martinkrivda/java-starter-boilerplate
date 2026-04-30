package com.example.javastarterboilerplate.api.controller;

import com.example.javastarterboilerplate.api.dto.ApplicationInfoResponse;
import com.example.javastarterboilerplate.api.dto.ServiceIndexResponse;
import com.example.javastarterboilerplate.application.ApplicationInfoService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@Tag(name = "Operations")
@Controller("/")
public class RootController {

  private final ApplicationInfoService applicationInfoService;

  public RootController(ApplicationInfoService applicationInfoService) {
    this.applicationInfoService = applicationInfoService;
  }

  @Get
  @Operation(
      summary = "Read service index",
      description = "Returns basic service metadata and links to operational endpoints.")
  public ServiceIndexResponse index() {
    ApplicationInfoResponse info = applicationInfoService.getInfo();
    return new ServiceIndexResponse(
        info.name(),
        info.version(),
        info.description(),
        "UP",
        Map.of(
            "info", "/rest/v1/info",
            "reference", "/reference",
            "openapi", "/doc",
            "health", "/health",
            "readiness", "/health/ready",
            "liveness", "/health/live",
            "metrics", "/metrics"));
  }
}
