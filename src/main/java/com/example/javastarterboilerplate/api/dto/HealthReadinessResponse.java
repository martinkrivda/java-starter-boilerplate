package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record HealthReadinessResponse(String status, List<HealthCheckResponse> checks) {}
