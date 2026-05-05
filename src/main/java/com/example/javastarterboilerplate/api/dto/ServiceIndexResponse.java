package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.Map;

/**
 * Root index response returned by {@code GET /}.
 *
 * <p>Provides a machine-readable entry point for service discovery with links to all well-known
 * operational endpoints.
 *
 * @param name application name
 * @param version deployed version
 * @param description short application description
 * @param status static {@code "UP"} string indicating the service is reachable
 * @param links map of endpoint names to their absolute paths
 */
@Serdeable
public record ServiceIndexResponse(
    String name, String version, String description, String status, Map<String, String> links) {}
