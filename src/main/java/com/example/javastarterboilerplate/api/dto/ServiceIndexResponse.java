package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

@Serdeable
public record ServiceIndexResponse(
    String name, String version, String description, String status, Map<String, String> links) {}
