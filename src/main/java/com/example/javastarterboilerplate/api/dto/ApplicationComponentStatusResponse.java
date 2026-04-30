package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ApplicationComponentStatusResponse(
    String component, boolean enabled, String detail) {}
