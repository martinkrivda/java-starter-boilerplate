package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

/**
 * Readiness probe response returned by {@code GET /health/ready}.
 *
 * @param status {@code "ready"} when the service can accept traffic, {@code "not_ready"} during
 *     drain
 * @param checks per-component check results that informed the readiness decision
 */
@Serdeable
public record HealthReadinessResponse(String status, List<HealthCheckResponse> checks) {}
