package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public record ApplicationInfoResponse(String name, String version, String description, String activeDatabaseProfile,
        List<String> activeEnvironments, List<ApplicationComponentStatusResponse> integrations) {
}
