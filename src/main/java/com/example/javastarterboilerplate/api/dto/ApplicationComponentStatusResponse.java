package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Status of a single optional integration component reported in health and info responses.
 *
 * @param component component identifier, e.g. {@code "storage"}, {@code "pdfbox"} or {@code "dss"}
 * @param enabled whether the component is configured and ready
 * @param detail human-readable status detail from the adapter
 */
@Serdeable
public record ApplicationComponentStatusResponse(
    String component, boolean enabled, String detail) {}
