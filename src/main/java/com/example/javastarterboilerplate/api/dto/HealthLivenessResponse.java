package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record HealthLivenessResponse(String status) {}
