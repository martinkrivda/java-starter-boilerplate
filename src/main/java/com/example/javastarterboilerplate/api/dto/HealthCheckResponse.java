package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

/**
 * Individual component check result included in health responses.
 *
 * @param name component identifier
 * @param status {@code "UP"} or {@code "DOWN"}
 * @param responseTimeMs response time or ordering hint reported in milliseconds
 * @param message human-readable status message from the component
 * @param details key-value map of additional diagnostic properties
 */
@Serdeable
public record HealthCheckResponse(
    String name, String status, long responseTimeMs, String message, Map<String, Object> details) {}
