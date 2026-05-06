package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

/**
 * Response payload for {@code GET /rest/v1/info} carrying application version and integration
 * readiness information.
 *
 * @param name application name
 * @param version deployed application version
 * @param description short application description
 * @param activeDatabaseProfile active persistence profile, e.g. {@code "h2"} or {@code
 *     "postgresql"}
 * @param activeEnvironments sorted list of active Micronaut environments
 * @param integrations status of optional integrations such as storage, PDFBox and DSS
 */
@Serdeable
public record ApplicationInfoResponse(
    String name,
    String version,
    String description,
    String activeDatabaseProfile,
    List<String> activeEnvironments,
    List<ApplicationComponentStatusResponse> integrations) {}
