package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Liveness probe response returned by {@code GET /healthz}.
 *
 * @param status always {@code "UP"} unless the JVM has crashed
 */
@Serdeable
public record HealthLivenessResponse(String status) {}
