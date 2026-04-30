package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
public record HealthCheckResponse(
    String name, String status, long responseTimeMs, String message, Map<String, Object> details) {}
