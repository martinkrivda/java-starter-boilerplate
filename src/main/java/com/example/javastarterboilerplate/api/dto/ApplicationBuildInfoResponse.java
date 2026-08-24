package com.example.javastarterboilerplate.api.dto;

import io.micronaut.serde.annotation.Serdeable;

/**
 * Build metadata for a deployed application artifact.
 *
 * @param number CI build or pipeline number
 * @param commit source control commit SHA used for the build
 * @param timestamp UTC build timestamp in ISO-8601 format
 */
@Serdeable
public record ApplicationBuildInfoResponse(String number, String commit, String timestamp) {}
