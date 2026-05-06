package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

/**
 * Full health check response returned by {@code GET /health}.
 *
 * @param status overall status string: {@code "UP"} or {@code "DOWN"}
 * @param version deployed application version
 * @param uptime seconds elapsed since the application started
 * @param checks per-component check results
 */
@Serdeable
public record HealthFullResponse(
    String status, String version, long uptime, List<HealthCheckResponse> checks) {}
