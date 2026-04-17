package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record HealthFullResponse(String status, String version, long uptime, List<HealthCheckResponse> checks) {
}
